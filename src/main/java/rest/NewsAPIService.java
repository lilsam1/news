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
	
	// ���� ���	- ���� ����� ���� �����͸� JSON ���·� HTTP Body�� ����
	@POST
	@Consumes(MediaType.APPLICATION_JSON)	// Ŭ���̾�Ʈ ��û�� ���Ե� �̵�� Ÿ���� ����. JSON�� ���
	public String addNews(News news) {
		try {
			dao.addNews(news);
			// @Consumes ������ ���� HTTP Body�� ���Ե� JSON ���ڿ��� �ڵ����� News�� ��ȯ
			// �̸� ���ؼ� JSON ���ڿ��� Ű�� News ��ü�� ����������� �����ؾ� ��
		} catch (Exception e) {
			e.printStackTrace();
			return "News API: ���� ��� ����";
		}
		return "News API: ���� ��ϵ�";
	}
	
	// ���� �� ����	- Ư�� ������ aid�� ��� �Ķ���ͷ� ����
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
	
	// ���� ���	- ��ü ���� ����� JSON ���·� ����
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
	
	// ���� ����	- ���� ������ ���� aid�� ��� �Ķ���ͷ� ����
	@DELETE
	@Path("{aid}")
	@Produces(MediaType.APPLICATION_JSON)
	public String DelNews(@PathParam("aid") int aid) {
		
		try {
			dao.delNews(aid);
		} catch (Exception e) {
			e.printStackTrace();
			return "News API: ���� ���� ����";
		}
		return "News API: ���� ������";
	}
}
