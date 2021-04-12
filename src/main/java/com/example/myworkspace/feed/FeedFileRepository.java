package com.example.myworkspace.feed;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface FeedFileRepository
	extends JpaRepository<FeedFile, Long>{
	
	// 피드에 파일 목록을 조회
	// FROM feed_file
	// WHERE feed_id = ?1
	List<FeedFile> findByFeedId(long feedId);
}
