package com.example.myworkspace.todo;

import java.util.Date;
import java.util.List;

import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class TodoController {
	// 목록조회, 추가, 수정, 삭제
	
	private TodoRepository todoRepo;
	
	@Autowired
	public TodoController(TodoRepository todoRepo) {
		this.todoRepo = todoRepo;
	}
	
	// todo 목록 조회
	@RequestMapping(value="/todos", method=RequestMethod.GET)
	public List<Todo> getTodos(){
		return todoRepo.findAll(); 
	}
	
	// 1건 추가
	@RequestMapping(value="/todos", method=RequestMethod.POST)
	public Todo addTodo(@RequestBody Todo todo) {
		todo.setDone(false);
		todo.setCreatedTime(new Date().getTime());
	
		todoRepo.save(todo); 
		return todo;
	}
	
	// 1건 isDone 수정
	@RequestMapping(value="/todos/{id}/done", method=RequestMethod.PATCH)
	public Todo modifyTodo(@PathVariable("id") long id, HttpServletResponse res) {
		
		Todo todo = todoRepo.findById(id).orElse(null);
		
		if(todo == null) {
			res.setStatus(HttpServletResponse.SC_NOT_FOUND);
			return null;
		}
		
		todo.setDone(true);
		todo.setModifiedTime(new Date().getTime());
		
		todoRepo.save(todo);
		
		return todo;
	}
	
	
	// 1건 삭제
	@RequestMapping (value="/todos/{id}",method=RequestMethod.DELETE)
	public boolean removeTodo(@PathVariable("id") long id, HttpServletResponse res) {

		Todo todo = todoRepo.findById(id).orElse(null);
		
		if(todo == null) {
			res.setStatus(HttpServletResponse.SC_NOT_FOUND);
			return false;
		}
		
		todoRepo.deleteById(id);
		
		return true;		
	}
}
