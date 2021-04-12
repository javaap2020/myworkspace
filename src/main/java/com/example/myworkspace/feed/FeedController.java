package com.example.myworkspace.feed;

import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.util.FileCopyUtils;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.example.myworkspace.configuration.ApiConfiguration;
import com.example.myworkspace.security.Auth;
import com.example.myworkspace.security.Profile;

@RestController
public class FeedController {

	private FeedRepository feedRepo;
	private FeedFileRepository feedFileRepo;
	private final Path FILE_PATH = Paths.get("feed_file");

	@Autowired
	private ApiConfiguration apiConfig;

	@Autowired // @Repository에 해당하는 인터페이스
				// 구현체(object, instance <- class)를
				// 생성해서 주입
	public FeedController(FeedRepository feedRepo, FeedFileRepository feedFileRepo) {
		this.feedRepo = feedRepo;
		this.feedFileRepo = feedFileRepo;
	}

	// feed 목록 조회
	/* @Auth */
	@RequestMapping(value = "/feeds", method = RequestMethod.GET)
	public List<Feed> getFeeds(HttpServletRequest req) {
		// return feedRepo.findAll(); // SELECT * FROM feed;
		// 전체 목록 조회, id 필드로 역정렬

//		System.out.println("-------- controller method profile ---------");
//		Profile profile = (Profile) req.getAttribute("profile");
//		System.out.println(profile);

		// 접속한 사용자의 작성한 글 보기
		// /users/kdkcom@naver.com/feeds ---- 매개변수값으로 조회하면 안 됨.
		// /users/feeds --- 세션에 저장되어있는 사용자 id 값으로 필터를 해야됨.

		// findByUserId(profile.getUserId()) --- 사용자가 작성한 글 보여주기

		// 사용자 userId와 팔로잉한 userId로 작성된 글 보기
		// WHERE user_id IN ('...', '...', '...', .....)
		// finByUserIdIn(List<String> userIds)

		List<Feed> list = feedRepo.findAll(Sort.by("id").descending());
		for (Feed feed : list) {
			for (FeedFile file : feed.getFiles()) {
				file.setDataUrl(apiConfig.getBasePath() + "/feed-files/" + file.getId());
			}
		}

		// test
		return list;
//		return new ArrayList<Feed>();
	}

	// 1건 추가
	@RequestMapping(value = "/feeds", method = RequestMethod.POST)
	public Feed addFeed(@RequestBody Feed feed) {
		feed.setCreatedTime(new Date().getTime());
		// repository.save(엔티티)
		// id값이 없을 때는 insert
		// 있을 때는 update
		feedRepo.save(feed);
		return feed;
	}

	// 1건 수정
	@RequestMapping(value = "/feeds/{id}", method = RequestMethod.PUT)

	public Feed modifyFeed

	(@PathVariable("id") long id, @RequestBody String content, HttpServletResponse res) {

		// SELECT ... FROM feed WHERE id = 1;
		// Feed feed = feedRepo.getOne(id);
		// 1. 수정할 id(PK)로 레코드를 조회한다.
		Feed feed = feedRepo.findById(id).orElse(null);

		if (feed == null) {
			res.setStatus(HttpServletResponse.SC_NOT_FOUND);
			return null;
		}
		// 2. 수정할 필드(컬럼)만 수정한다.
		feed.setContent(content);
		feed.setModifiedTime(new Date().getTime());

		// {"content":"수정한 내용"}
		// content: 수정한 내용, create_time: 0, name: "", ..

		// 모든 필드(컬럼)을 업데이트함
		// 3. save(update)를 한다.
		feedRepo.save(feed);

		return feed;
	}

	// 1건 삭제
	@RequestMapping(value = "/feeds/{id}", method = RequestMethod.DELETE)
	public boolean removeFeed(@PathVariable("id") long id, HttpServletResponse res) {
		System.out.println(id);

		// SELECT ... FROM feed WHERE id = 1;
		// Feed feed = feedRepo.getOne(id);
		// 1. 수정할 id(PK)로 레코드를 조회한다.
		Feed feed = feedRepo.findById(id).orElse(null);

		if (feed == null) {
			res.setStatus(HttpServletResponse.SC_NOT_FOUND);
			return false;
		}

		// hard-delete
		// feedRepo.delete(feed);

		// 하위 테이블 삭제
		List<FeedFile> files = feedFileRepo.findByFeedId(id);
		for (FeedFile file : files) {
			feedFileRepo.delete(file);
		}

		feedRepo.deleteById(id);
		/*
		 * // soft-delete feed.setDeletedTime(new Date()); // 모든 필드(컬럼)을 업데이트함 // 3.
		 * save(update)를 한다. feedRepo.save(feed);
		 * 
		 * return feed;
		 */
		return true;
	}

	// 작성자 이름으로 피드 목록을 조회
	// GET /feeds/search/name?keyword=고대근
	@RequestMapping(value = "/feeds/search/name", method = RequestMethod.GET)
	public List<Feed> getFeedsByName(@RequestParam("keyword") String keyword) {
		return feedRepo.findByName(keyword);
	}

	// 시간 값보다 이전에 작성한 피드 목록을 조회
	// GET /feeds/search/created-time?end=1600030003
	@RequestMapping(value = "/feeds/search/created-time", method = RequestMethod.GET)
	public List<Feed> getFeedsByCreatedTimeLessThan(@RequestParam("end") long end) {
		return feedRepo.findByCreatedTimeLessThan(end);
	}

	// id로 역정렬
	@RequestMapping(value = "/feeds/sort", method = RequestMethod.GET)
	public List<Feed> getFeedsSorted() {
		// 전체 목록 조회, id 필드로 역정렬
		return feedRepo.findAll(Sort.by("id").descending());
	}

	// 페이징

	// 데이터가 23건
	// 0페이지 10건
	// 1페이지 10건
	// 2페이지 10건 (3건) pageSize 작음.. 그다음 로딩
	// 3페이지 10건 X 로딩X
	// pageSize = 10 고정 클라이언트로 부터 받음
	// page = 0~...n 클라이언트로부터 페이지를 받음

	// GET /feeds/paging?page=0&size=10
	// GET /feeds/paging?page=1&size=10
	// GET /feeds/paging?page=2&size=10 -> 3건 밑에 데이터 더이상 X
	@RequestMapping(value = "/feeds/paging", method = RequestMethod.GET)
	public List<Feed> getFeedsPaging(@RequestParam("page") int page, @RequestParam("size") int size) {
		// 전체 목록 조회, 페이징
		return feedRepo.findAll(PageRequest.of(page, size)).toList();
	}

	@RequestMapping(value = "/feeds/paging-and-sort", method = RequestMethod.GET)
	public Page<Feed> getFeedsPagingAndSort(@RequestParam("page") int page, @RequestParam("size") int size) {
		// 전체 목록 조회, 페이징, id 역정렬
		return feedRepo.findAll(PageRequest.of(page, size, Sort.by("id").descending()));
	}

	// 작성자 이름으로 피드 목록을 조회 + id 역정렬 + 페이징
	// GET /feeds/search/name-sort-paging?keyword=고대근&page=0&size=2
	@RequestMapping(value = "/feeds/search/name-sort-paging", method = RequestMethod.GET)
	public List<Feed> getFeedsByNameSortPaging(@RequestParam("keyword") String keyword, @RequestParam("page") int page,
			@RequestParam("size") int size) {
		return feedRepo.findByName(keyword, PageRequest.of(page, size, Sort.by("id").descending()));
	}

	// GET /feeds/search/content?query=내용&page=0&size=2
	@RequestMapping(value = "/feeds/search/content", method = RequestMethod.GET)
	public List<Feed> getFeedsByContentContaining(@RequestParam String query, @RequestParam int page,
			@RequestParam int size)

	{
		System.out.println(query);
		System.out.println(page);
		System.out.println(size);

		return feedRepo.findByContentContaining(query, PageRequest.of(page, size, Sort.by("id").descending()));
	}

	// {id}인 feed 파일 1개 추가
	@RequestMapping(value = "/feeds/{id}/feed-files", method = RequestMethod.POST)
	public FeedFile addFeedFile(@PathVariable("id") long id, @RequestPart("data") MultipartFile file,
			HttpServletResponse res) throws IOException {

		if (feedRepo.findById(id).orElse(null) == null) {
			res.setStatus(HttpServletResponse.SC_NOT_FOUND);
			return null;
		}

		System.out.println(file.getOriginalFilename());

		// 디렉토리가 없으면 생성
		if (!Files.exists(FILE_PATH)) {
			Files.createDirectories(FILE_PATH);
		}

		// 파일 저장
		FileCopyUtils.copy(file.getBytes(), new File(FILE_PATH.resolve(file.getOriginalFilename()).toString()));
		// 파일 메타 데이터 저장
		FeedFile feedFile = FeedFile.builder().feedId(id).fileName(file.getOriginalFilename())
				.contentType(file.getContentType()).build();

		feedFileRepo.save(feedFile);
		feedFile.setDataUrl(apiConfig.getBasePath() + "/feed-files/" + feedFile.getId());
		return feedFile;
	}

	// {id}인 feed에 feed-files 목록 조회
	@RequestMapping(value = "/feeds/{id}/feed-files", method = RequestMethod.GET)
	public List<FeedFile> getFeedFiles(@PathVariable("id") long id, HttpServletResponse res) {

		if (feedRepo.findById(id).orElse(null) == null) {
			res.setStatus(HttpServletResponse.SC_NOT_FOUND);
			return null;
		}

		List<FeedFile> feedFiles = feedFileRepo.findByFeedId(id);
		for (FeedFile file : feedFiles) {
			file.setDataUrl(apiConfig.getBasePath() + "/feed-files/" + file.getId());
		}
		System.out.println(feedFiles);

		return feedFiles;
	}

	// {id}인 feed에 feed-files 목록 삭제
	@RequestMapping(value = "/feeds/{id}/feed-files", method = RequestMethod.DELETE)
	public boolean removeFeedFiles(@PathVariable("id") long id, HttpServletResponse res) {

		if (feedRepo.findById(id).orElse(null) == null) {
			res.setStatus(HttpServletResponse.SC_NOT_FOUND);
			return false;
		}

		List<FeedFile> feedFiles = feedFileRepo.findByFeedId(id);
		for (FeedFile feedFile : feedFiles) {
			feedFileRepo.delete(feedFile);
			File file = new File(feedFile.getFileName());
			if (file.exists()) {
				file.delete();
			}
		}

		return true;
	}

	@RequestMapping(value = "/feed-files/{id}", method = RequestMethod.GET)
	public ResponseEntity<byte[]> getFeedFile(@PathVariable("id") long id, HttpServletResponse res) throws IOException {
		FeedFile feedFile = feedFileRepo.findById(id).orElse(null);

		if (feedFile == null) {
			return ResponseEntity.notFound().build();
		}

		HttpHeaders responseHeaders = new HttpHeaders();
		responseHeaders.set("Content-Type", feedFile.getContentType() + ";charset=UTF-8");
		// inline: 뷰어로, attachement: 내려받기
		responseHeaders.set("Content-Disposition",
				"inline; filename=" + URLEncoder.encode(feedFile.getFileName(), "UTF-8"));

		return ResponseEntity.ok().headers(responseHeaders)
				.body(Files.readAllBytes(FILE_PATH.resolve(feedFile.getFileName())));
	}

}
