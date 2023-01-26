package model;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class NewsDAO {
	final String JDBC_DRIVER = "org.mariadb.jdbc.Driver";
	final String JDBC_URL = "jdbc:mariadb://localhost:3308/news";
	final String JDBC_USER = "root";
	final String JDBC_PASSWORD ="1475";
	
	public Connection open() {
		// DB 연결을 가져오는 메서드
		// 각각의 메서드마다 연결을 만들고 해제하는 구조
		
		Connection connection = null;
		try {
			Class.forName(JDBC_DRIVER);
			connection = DriverManager.getConnection(JDBC_URL, JDBC_USER, JDBC_PASSWORD);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return connection;
	}
	
	public void addNews(News news) throws Exception {
		// 뉴스를 추가하는 메서드
		Connection connection = open();
		
		String sql = "INSERT INTO news (title, img, date, content) VALUES (?, ?, now(), ?)";
		PreparedStatement preparedStatement = connection.prepareStatement(sql);
		
		// try-with-resource 기법이 적용된 부분으로 해당 리소스를 자동으로 close
		try(connection; preparedStatement) {
			preparedStatement.setString(1, news.getTitle());
			preparedStatement.setString(2, news.getImg());
			preparedStatement.setString(3, news.getContent());
			preparedStatement.executeUpdate();
		}
	}
	
	public List<News> getAll() throws Exception {
		// 뉴스 기사 목록 전체를 가지고 오기 위한 메서드
		Connection connection = open();
		List<News> newsList = new ArrayList<>();
		
		String sql = "SELECT aid, title, date FROM news";
		PreparedStatement preparedStatement = connection.prepareStatement(sql);
		ResultSet resultSet = preparedStatement.executeQuery();
		
		try(connection; preparedStatement; resultSet) {
			while (resultSet.next()) {
				News news = new News();
				news.setAid(resultSet.getInt("aid"));
				news.setTitle(resultSet.getString("title"));
				news.setDate(resultSet.getString("date"));
				newsList.add(news);
			}
		}		
		return newsList;
	}
	
	public News getNews(int aid) throws Exception {
		// 뉴스 목록에서 뉴스를 선택했을 때 특정 뉴스 기사의 세부 내용을 보여주기 위한 메서드
		Connection connection = open();
		
		News news = new News();
		String sql = "SELECT aid, title, img, date, content FROM news WHERE aid = ?";
		PreparedStatement preparedStatement = connection.prepareStatement(sql);
		preparedStatement.setInt(1, aid);
		ResultSet resultSet = preparedStatement.executeQuery();
		resultSet.next();
		
		try(connection; preparedStatement; resultSet) {
			news.setAid(resultSet.getInt("aid"));
			news.setTitle(resultSet.getString("title"));
			news.setImg(resultSet.getString("img"));
			news.setDate(resultSet.getString("date"));
			news.setContent(resultSet.getString("content"));
			return news;
		}
	}
	
	public void delNews(int aid) throws Exception {
		// 뉴스 삭제
		Connection connection = open();
		String sql = "DELETE FROM news WHERE aid = ?";
		PreparedStatement preparedStatement = connection.prepareStatement(sql);
		try(connection; preparedStatement) {
			preparedStatement.setInt(1, aid);
			// 삭제된 뉴스 기사가 없을 경우
			if(preparedStatement.executeUpdate() == 0) {
				throw new SQLException("DB에러");
			}
		}
	}
	
}
