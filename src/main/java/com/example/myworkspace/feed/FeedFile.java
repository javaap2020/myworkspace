package com.example.myworkspace.feed;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Transient;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;

import com.example.myworkspace.configuration.ApiConfiguration;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data @Builder @AllArgsConstructor @NoArgsConstructor
@Entity
// 테이블명: feed_file
public class FeedFile {
	
	@Id @GeneratedValue(strategy = GenerationType.IDENTITY)
	private long id;
	private String fileName;
	private String contentType;
	private long feedId;

	@Transient
	private String dataUrl;
}
