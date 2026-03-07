import React, { useState, useEffect } from "react";
import newsApi from "./api/newsApi";
import SearchForm from "./components/SearchForm";
import NewsList from "./components/NewsList";
import { Client } from "@stomp/stompjs";
import SockJS from "sockjs-client";

export default function App() {

  const [data, setData] = useState({ articles: [], totalPages: 1 });
  const [page, setPage] = useState(1);
  const [keyword, setKeyword] = useState("latest-news");
  const [pageSize, setPageSize] = useState(10);
  const [loading, setLoading] = useState(false);

  const searchNews = async (q, p, ps) => {

    setLoading(true);

    try {
      const result = await newsApi.search(q, p, ps, false);

      setKeyword(q);
      setPage(p);
      setPageSize(ps);

      setData(prev => ({
        ...result,
        articles: p === 1
          ? result.articles
          : [...prev.articles, ...result.articles]
      }));

    } catch (err) {
      console.error("Error fetching news", err);
    }

    setLoading(false);
  };

  const loadMore = () => {

    if (page >= data.totalPages) return;

    const nextPage = page + 1;
    searchNews(keyword, nextPage, pageSize);
  };

  // Initial load
  useEffect(() => {
    searchNews(keyword, 1, pageSize);
  }, []);

  // WebSocket connection
  useEffect(() => {

    const client = new Client({
      webSocketFactory: () => new SockJS("/news-socket"),
      reconnectDelay: 5000,
      debug: (str) => console.log(str)
    });

    client.onConnect = () => {

      console.log("WebSocket Connected");

      client.subscribe("/topic/news", (message) => {

        const latestNews = JSON.parse(message.body);

        console.log("Received latest news update");

        setData(latestNews);  // replace data

      });

    };

    client.activate();

    return () => {
      client.deactivate();
    };

  }, []);

  return (
    <>
      <SearchForm onSearch={searchNews} />

      <NewsList
        data={data}
        loading={loading}
        onLoadMore={loadMore}
      />
    </>
  );
}