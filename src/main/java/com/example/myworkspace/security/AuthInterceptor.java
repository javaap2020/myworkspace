package com.example.myworkspace.security;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.HashOperations;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.handler.HandlerInterceptorAdapter;

// request 들어왔을 때 컨트롤러로 가기전의 사전 처리 
// 컨트롤러에서 처리 된 후의 사후 처리
@SuppressWarnings("deprecation")
public class AuthInterceptor extends HandlerInterceptorAdapter {
	
	@Autowired
	private StringRedisTemplate redisTemplate;
	
	
	@Override
	public boolean preHandle(HttpServletRequest req, HttpServletResponse res, Object handler) {
		System.out.println("-------- interceptor --------");
		
		// 특정 controller의 메소드에만 인증체크
		
		// 1. 컨트롤러의 리퀘스트 핸들러 메소드가 아니면 인증 체크 제외
		if(handler instanceof HandlerMethod == false) {
			return true;
		}
		
		// 2. 형 변환
		HandlerMethod handlerMethod = (HandlerMethod) handler;
		
		// 3. @Auth 받아오기
		Auth auth = handlerMethod.getMethodAnnotation(Auth.class);
		
		// 4. @Auth 어노테이션이 없으면 인증 체크 필요 없음
		if(auth == null) {
			return true;
		}
		
		// 5. @Auth 어노테이션 있으면 세션 확인
		System.out.println(req.getHeader("authorization"));
		String sessionId = req.getHeader("authorization").replace("Bearer ", "");
		Profile profile = getSessionProfile(sessionId);
		
		if(profile == null) {
			res.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
			return false;
		}
		
		// 리퀘스트 객체에 속성을 추가
		req.setAttribute("profile", profile);
		
		// 세션이 있으면 통과
		return true;
	}
	
	// session id로 프로필 정보를 조회
	private Profile getSessionProfile(String sessionId) {
		HashOperations<String, String, String> record = redisTemplate.opsForHash();
		String profileId = record.get(sessionId, "id");
		if(profileId == null) {
			return null;
		}
		
		return Profile.
				builder()
					.id(Long.valueOf(profileId))
					.userType(record.get(sessionId, "userType"))
					.userId(record.get(sessionId, "userId"))
					.name(record.get(sessionId, "name"))
					.email(record.get(sessionId, "email"))
					.image(record.get(sessionId, "image"))
				.build();
	}	
}
