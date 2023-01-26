package controller;

import java.io.IOException;
import java.lang.reflect.Method;
import java.sql.SQLException;
import java.util.List;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.annotation.MultipartConfig;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.Part;

import org.apache.commons.beanutils.BeanUtils;

import model.News;
import model.NewsDAO;

@WebServlet("*.nhn")
// 뉴스 이미지 파일 업로드 처리를위해 @MultipartConfig을 추가
// 최대 파일 크기와 저장 위치를 지정
@MultipartConfig(maxFileSize = 1024 * 1024 * 2, location = "c:/img")
public class NewsController extends HttpServlet {
	private NewsDAO dao;
	private ServletContext ctx;	// 서버 로그 메세지 생성을 위해 선언
	
	// 웹 리소스 기본 경로 지정
	private final String START_PAGE = "ver01/newsList.jsp";
	
	public void init(ServletConfig config) throws ServletException {
		// init() 메서드에서 초기화 진행
		super.init(config);
		dao = new NewsDAO();
		ctx = getServletContext();
	}

	@Override
	protected void service(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		// TODO Auto-generated method stub
		req.setCharacterEncoding("utf-8");
		String action = req.getParameter("action");
		
		dao = new NewsDAO();
		
		// 자바 리플렉션을 사용해 if, switch 없이 요청에 따라 구현 메서드가 실행되도록 구성
		// 자바의 리플랙션 API를 사용해 자동으로 action에 지정된 이름과 동일한 메서드를 호출하는 구조
		// 불필요한 if 처리를 줄여주고 구조 변경에 유리한 방법
		Method m;
		String view = null;
		
		// action 파라미터 없이 접근한 경우
		// 컨트롤러 실행 시 자동으로 시작 페이지로 이동
		if (action == null) {
			action = "listNews";
		}
		System.out.println(action);
		// 1. 자바 리플렉션으로 action으로 전달된 이름의 메서드 자동으로 호출
		try {
			// 현재 클래스에서 action 이름과 HttpServletRequest를 파라미터로 하는 메서드 찾음
			m = this.getClass().getMethod(action, HttpServletRequest.class);
			// this.getClass() : 현재 클래스 객체에 대한 참조
			// getMethod() : 첫 번쨰 인자로 메서드 이름, 두 번쨰 인자 타입을 메서드의 인자로 가지는 메서드 객체를 가져옴
			
			// 메서드 실행 후 리턴값 받아옴
			view = (String) m.invoke(this, req);
			
		} catch (NoSuchMethodException e) {
			e.printStackTrace();
			ctx.log("요청 action 없음");	// 톰캣의 콘솔에 로드 메세지를 남기는 메서드
			req.setAttribute("error", "action 파라미터가 잘못되었습니다");
			view = START_PAGE;
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		// 2. 뷰 이동
		// 요청 처리 메서드를 호출한 다음 리턴된 뷰 페이지로 이동
		
		// view 문자열이 "redirect:/" 문자열로 시작하는 경우 "redirect:/" 이후의 경로만
		// 들고와 resp.sendRedirect()를 이용해 페이지 이동
		if (view.startsWith("redirect:/")) {
			String rview = view.substring("redirect:/".length());	// redirect:/ 문자열 이후의 경로만 가져옴
			resp.sendRedirect(rview);
		}
		else {
			RequestDispatcher requestDispatcher = req.getRequestDispatcher(view);
			requestDispatcher.forward(req, resp);	// 지정된 뷰로 포워딩
		}
	}
	
	private String getFilname(Part part) {
		// Part 객체를 사용해 multipart 헤더에서 파일이름 추출
		String fileName = null;
		// 파일 이름이 들어있는 헤더 영역을 가져옴
		String header = part.getHeader("content-disposition");
		// part.getHeader -> form-data; name="file"; filename="사진5.jpg"
		ctx.log(" Filed Header : " + header);
		
		// 파일 이름이 들어있는 속성 부분의 시작위치를 가져와 쌍따옴표 사이의 값 부분만 가지고 옴
		int start = header.indexOf("filename");
		fileName = header.substring(start + 10, header.length()-1);
		ctx.log("파일명:" + fileName);
		return fileName;
	}
	
	public String addNews(HttpServletRequest request) {
		// 뉴스 기사 등록하기 위한 요청을 처리하는 메서드
		News news = new News();
		try {
			// 이미지 파일 저장을 위해 request로부터 Part 객체 참조
			Part part = request.getPart("file");
			String fileName = getFilname(part);
			if (fileName != null  && !fileName.isEmpty()) {
				part.write(fileName);	// 파일 이름이 있으면 파일 저장
			}
			// BeanUtils.populate() : 파라미터로 전달된 name 속성과 일치하는 news 클래스의 멤버 변수를 찾아 값을 전달
			BeanUtils.populate(news, request.getParameterMap());
			// 이미지 파일 이름을 News 객체에도 저장
			news.setImg("/img/" + fileName);
			
			dao.addNews(news);
		} catch (Exception e) {
			e.printStackTrace();
			ctx.log("뉴스 추가 과정에서 문제 발생");
			request.setAttribute("error", "뉴스가 정상적으로 등록되지 않았씁니다");
			return listNews(request);
		}
		return "redirect:/news.nhn?action=listNews";
	}
	
	public String listNews(HttpServletRequest request) {
		// newsList.jsp에서 뉴스 목록을 보여주기 위한 요청을 처리하는 메서드
		List<News> list;
		try {
			list = dao.getAll();
			request.setAttribute("newslist", list);
		} catch (Exception e) {
			e.printStackTrace();
			ctx.log("뉴스 목록 생성 과정에서 문제 발생");
			request.setAttribute("error", "뉴스 목록이 정상적으로 처리되지 않았습니다");
		}
		return "./newsList.jsp";
	}
	
	public String getNews(HttpServletRequest request) {
		// 특정 뉴스 기사를 클릭했을 때  호출하기 위한 요청을 처리하는 메서드
		int aid = Integer.parseInt(request.getParameter("aid"));
		try {
			News n = dao.getNews(aid);
			request.setAttribute("news", n);
		} catch (Exception e) {
			e.printStackTrace();
			ctx.log("뉴스를 가져오는 과정에서 문제 발생");
			request.setAttribute("error", "뉴스를 정상적으로 가져오지 못했습니다");
		}
		return "./newsView.jsp";
	}
	
	public String deleteNews(HttpServletRequest request) {
		int aid = Integer.parseInt(request.getParameter("aid"));
		try {
			dao.delNews(aid);
		} catch (SQLException e) {
			e.printStackTrace();
			ctx.log("뉴스 삭제 과정에서 문제 발생");
			request.setAttribute("error", "뉴스가 정상적으로 삭제되지 않았습니다");
			return listNews(request);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
		
		return "redirect:/news.nhn?action=listNews";
	}
}
