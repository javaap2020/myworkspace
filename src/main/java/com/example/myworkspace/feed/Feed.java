package com.example.myworkspace.feed;

import java.util.Date;
import java.util.List;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.OneToMany;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;


//ORM(Object Relational Mapping)
//프로그래밍 객체 - 데이터베이스 객체(테이블)
@Data @Builder @AllArgsConstructor @NoArgsConstructor
@Entity // Entity(SE) - 데이터를 처리하는(저장하는) 객체
        // object(SE) - entity(데이터), controller(제어), boundary(경계)
//테이블명: feed
public class Feed {
	// @Id: PK(primary key)
	// @GeneratedValue: 값 생성 방식
	// strategy = GenerationType.IDENTITY: auto_increment
	@Id @GeneratedValue(strategy = GenerationType.IDENTITY)
	private long id;
	
	@Column(columnDefinition = "TEXT") // TEXT
	private String content; 
	private String name;   // varchar(255)
		
	// 추가 필드
	private String userId;
	@Column(columnDefinition = "TEXT") // TEXT
	private String profileImage;
	
	// column: created_time - snake-case
	private long createdTime; // bigint - camel-case
	private long modifiedTime; // bigint
	private Date deletedTime; // 삭제 시간
	
	// 추가필드 
	// 하위 객체 목록
	@OneToMany
	@JoinColumn(name="feedId")
	private List<FeedFile> files;
}
