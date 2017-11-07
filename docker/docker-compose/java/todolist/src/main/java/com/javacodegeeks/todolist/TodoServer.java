package com.javacodegeeks.todolist;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;

import org.apache.commons.io.IOUtils;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.lambdaworks.redis.RedisClient;
import com.lambdaworks.redis.RedisConnection;
import com.lambdaworks.redis.ValueScanCursor;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;

public class TodoServer {

	public static void main(String[] args) throws Exception {
		HttpServer server = HttpServer.create(new InetSocketAddress(8000), 0);
		server.createContext("/", new MyHandler(System.getenv("REDIS_PORT")));
		server.setExecutor(null); // creates a default executor
		server.start();
	}

	static class MyHandler implements HttpHandler {

		private RedisClient redisClient;
		private RedisConnection<String, String> connection;
		private ObjectMapper mapper;

		public MyHandler(String redisURL) throws MalformedURLException {

			String hostPortURL = redisURL.substring("tcp://".length());
			int separator = hostPortURL.indexOf(':');
			redisClient = new RedisClient(hostPortURL.substring(0, separator),
					Integer.parseInt(hostPortURL.substring(separator + 1)));
			connection = redisClient.connect();
			mapper = new ObjectMapper();
		}

		public void handle(HttpExchange t) throws IOException {
			String method = t.getRequestMethod();
			OutputStream os = t.getResponseBody();
			String response = "";
			
			if (t.getRequestURI().getPath().equals("/todos")) {
				if (method.equals("GET")) {
					ValueScanCursor<String> cursor = connection.sscan("todos");
					List<String> tasks = cursor.getValues();
					response = mapper.writeValueAsString(tasks);

				} else if (method.equals("PUT")) {

					connection.sadd("todos", IOUtils.toString(t.getRequestBody()));
				}
			}

			t.sendResponseHeaders(200, response.length());
			os.write(response.getBytes());
			os.close();
		}

		@Override
		public void finalize() {
			connection.close();
			redisClient.shutdown();
		}
	}
}
