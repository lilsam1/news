package rest;

import java.util.List;

import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import model.News;
import model.NewsDAO;

@Path("/news")
public class NewsAPIService {
	NewsDAO dao;
	
	public NewsAPIService() {
		dao = new NewsDAO();
	}
	
	// 뉴스 등록	- 뉴스 등록을 위한 데이터를 JSON 형태로 HTTP Body로 전달
	@POST
	@Consumes(MediaType.APPLICATION_JSON)	// 클라이언트 요청에 포함된 미디어 타입을 지정. JSON을 사용
	public String addNews(News news) {
		try {
			dao.addNews(news);
			// @Consumes 설정에 따라 HTTP Body에 포함된 JSON 문자열이 자동으로 News로 변환
			// 이를 위해서 JSON 문자열의 키와 News 객체의 멤버변수명이 동일해야 함
		} catch (Exception e) {
			e.printStackTrace();
			return "News API: 뉴스 등록 실패";
		}
		return "News API: 뉴스 등록됨";
	}
	
	// 뉴스 상세 정보	- 특정 뉴스의 aid를 경로 파라미터로 전달
	@GET
	@Path("{aid}")
	@Produces(MediaType.APPLICATION_JSON)
	public News getNews(@PathParam("aid") int aid) {
		News news = null;
		
		try {
			news = dao.getNews(aid);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return news;
	}
	
	// 뉴스 목록	- 전체 뉴스 목록을 JSON 형태로 전달
	@GET
	@Produces(MediaType.APPLICATION_JSON)
	public List<News> getAll() {
		List<News> news = null;
		
		try {
			news = dao.getAll();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return news;
	}
	
	// 뉴스 삭제	- 뉴스 삭제를 위한 aid를 경로 파라미터로 전달
	@DELETE
	@Path("{aid}")
	@Produces(MediaType.APPLICATION_JSON)
	public String DelNews(@PathParam("aid") int aid) {
		
		try {
			dao.delNews(aid);
		} catch (Exception e) {
			e.printStackTrace();
			return "News API: 뉴스 삭제 실패";
		}
		return "News API: 뉴스 삭제됨";
	}
}
