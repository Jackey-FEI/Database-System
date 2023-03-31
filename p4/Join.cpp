#include "Join.hpp"

#include <vector>
#include <iostream>

using namespace std;

/* 
 * **********************************************************
 *                      Partition Phase
 * **********************************************************
 */

void partition_hash_helper(Disk* disk, Mem* mem, uint begin, uint end,
						vector<Bucket> &partitions,
						void (Bucket::*function_ptr)(uint)) {
	mem->reset();

	for (uint i = begin; i < end; i++) {
		// Load disk page to the input mem page
		mem->loadFromDisk(disk, i, MEM_SIZE_IN_PAGE - 1);
		Page *input_page = mem->mem_page(MEM_SIZE_IN_PAGE - 1);

		// Hash each records
		for (uint j = 0; j < input_page->size(); j++) {

			// Get record
			Record record = input_page->get_record(j);

			// Calculate hash_value and find the bucket page
			uint hash_value = record.partition_hash() % (MEM_SIZE_IN_PAGE - 1);
			Page* bucket_page = mem->mem_page(hash_value);

			// If the bucket page is full, flush it to the disk
			if (bucket_page->full()) {
				uint page_id = mem->flushToDisk(disk, hash_value);
				(partitions[hash_value].*function_ptr)(page_id);
				bucket_page->reset();
			}

			// Add the record to the corresponding bucket page
			bucket_page->loadRecord(record);
		}
	}

	// Load remaining bucket pages to the disk
	for (uint i = 0; i < MEM_SIZE_IN_PAGE - 1; i++) {
		(partitions[i].*function_ptr)(mem->flushToDisk(disk, i));
	}
}

/*
 * Input: Disk, Memory, Disk page ids for left relation, Disk page ids for right relation
 * Output: Vector of Buckets of size (MEM_SIZE_IN_PAGE - 1) after partition
 */
vector<Bucket> partition(Disk* disk, Mem* mem, pair<uint, uint> left_rel,
                         pair<uint, uint> right_rel) {
	
	vector<Bucket> partitions(MEM_SIZE_IN_PAGE - 1, Bucket(disk));

	// Left hash
	partition_hash_helper(disk, mem, left_rel.first, left_rel.second,
							partitions, &Bucket::add_left_rel_page);

	// Right hash
	partition_hash_helper(disk, mem, right_rel.first, right_rel.second,
							partitions, &Bucket::add_right_rel_page);
	return partitions;
}






/* 
 * **********************************************************
 *                        Probe Phase
 * **********************************************************
 */

void load_left_outer(Page* page, const Record& inner, const Record& outer) {
	page->loadPair(outer, inner);
}

void load_right_outer(Page* page, const Record& inner, const Record& outer) {
	page->loadPair(inner, outer);
}

void probe_match_helper(Disk* disk, Mem* mem, vector<Bucket>& partitions,
						Bucket& bucket, vector<uint> disk_pages,
						vector<uint> (Bucket::*get_outer_rel)(),
						vector<uint> (Bucket::*get_inner_rel)(),
						void (*load_pair)(Page*, const Record&, const Record&)) {
	vector<uint> pages = (bucket.*get_outer_rel)();
	Page *input_page = mem->mem_page(MEM_SIZE_IN_PAGE - 1);
	Page *output_page = mem->mem_page(MEM_SIZE_IN_PAGE - 2);

	// It is assumed that the smaller relation always fits in memory
	for (uint i = 0; i < pages.size(); i++) {
		mem->loadFromDisk(disk, pages[i], MEM_SIZE_IN_PAGE - 1);

		for (uint j = 0; j < input_page->size(); j++) {
			// Get record
			Record record = input_page->get_record(j);

			// Calculate hash_value and find the bucket page
			uint hash_value = record.probe_hash() % (MEM_SIZE_IN_PAGE - 2);
			Page* bucket_page = mem->mem_page(hash_value);

			// Add the record to the corresponding bucket page
			bucket_page->loadRecord(record);
		}
	}

	pages = (bucket.*get_inner_rel)();

	for (uint i = 0; i < pages.size(); i++) {
		mem->loadFromDisk(disk, pages[i], MEM_SIZE_IN_PAGE - 1);

		for (uint j = 0; j < input_page->size(); j++) {
			// Get record
			Record record = input_page->get_record(j);

			// Calculate hash_value and find the bucket page
			uint hash_value = record.probe_hash() % (MEM_SIZE_IN_PAGE - 2);
			Page* bucket_page = mem->mem_page(hash_value);

			for (uint m = 0; m < bucket_page->size(); m++) {
				if (record == bucket_page->get_record(m)) {

					// If the ouput page if full, flush it to the disk
					if (output_page->full()) {
						uint page_id = mem->flushToDisk(disk, MEM_SIZE_IN_PAGE - 2);
						disk_pages.push_back(page_id);
						output_page->reset();
					}

					// Update the output page with the new joined pair
					(*load_pair)(output_page, record, bucket_page->get_record(m));
				}
			}
		}
	}
}

/*
 * Input: Disk, Memory, Vector of Buckets after partition
 * Output: Vector of disk page ids for join result
 */
vector<uint> probe(Disk* disk, Mem* mem, vector<Bucket>& partitions) {

	vector<uint> disk_pages; // placeholder

	mem->reset();
	for (auto &bucket: partitions) {
		for (uint i = 0; i < MEM_SIZE_IN_PAGE - 2; i++) {
			mem->mem_page(i)->reset();
		}

		if (bucket.num_left_rel_record < bucket.num_right_rel_record) {
			probe_match_helper(disk, mem, partitions, bucket, disk_pages,
								&Bucket::get_left_rel, &Bucket::get_right_rel,
								&load_left_outer);
		} else {
			probe_match_helper(disk, mem, partitions, bucket, disk_pages,
								&Bucket::get_right_rel, &Bucket::get_left_rel,
								&load_right_outer);
		}
	}

	// Load remaining output page to the disk
	uint page_id = mem->flushToDisk(disk, MEM_SIZE_IN_PAGE - 2);
	disk_pages.push_back(page_id);
	return disk_pages;
}
