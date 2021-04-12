package com.example.myworkspace.feed;

import java.util.List;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

// @Repository: entity를 처리하는 객체
@Repository
public interface FeedRepository
					// <entity타입, id타입>
	extends JpaRepository<Feed, Long> {

	// findByName
	// find: select ... from .. 
	// ByName: where name = ?1(매개변수값)
	// select ... from feed WHERE name = ?1;
	
	/* https://docs.spring.io/spring-data/jpa/docs/1.5.0.RELEASE/reference/html/jpa.repositories.html */
	/* 2.3.2 Query creation */
	public List<Feed> findByName(String name);
	public List<Feed> findByName(String name, Pageable pageable);
	// select ... from feed where created_time < ?1
	// findBy+필드명+상세조건연산자
	// findByCreatedTimeLessThan
	public List<Feed> findByCreatedTimeLessThan(Long createdTime);
	
	// where content LIKE '%'?1'%'
	public List<Feed> findByContentContaining
						(String content, Pageable pageable);
}
