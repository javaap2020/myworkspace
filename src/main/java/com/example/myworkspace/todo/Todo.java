package com.example.myworkspace.todo;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data @Builder @AllArgsConstructor @NoArgsConstructor
@Entity
public class Todo {

	// id, memo, isDone(boolean), created_time
	// isDone은 "done" : true 이렇게 줘야함 
	
	@Id @GeneratedValue(strategy = GenerationType.IDENTITY)
	private long id;
	
	@Column(columnDefinition = "TEXT")
	private String memo;
	private boolean isDone;
	private long createdTime;
	private long modifiedTime;
	
}
