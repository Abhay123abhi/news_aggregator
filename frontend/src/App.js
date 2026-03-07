import React, { useState } from "react";
import newsApi from "./api/newsApi";
import SearchForm from "./components/SearchForm";
import NewsList from "./components/NewsList";

export default function App() {

  const [data, setData] = useState({ articles: [] });
  const [page, setPage] = useState(1);
  const [keyword, setKeyword] = useState("latest-news");
  const [pageSize, setPageSize] = useState(10);
  const [loading, setLoading] = useState(false);

  const searchNews = async (q, p, ps) => {

    setLoading(true);

    const result = await newsApi.search(q, p, ps, false);

    setKeyword(q);
    setPage(p);
    setPageSize(ps);

    setData({
      ...result,
      articles: p === 1
        ? result.articles
        : [...data.articles, ...result.articles]
    });

    setLoading(false);
  };

  const loadMore = () => {

    if (page >= data.totalPages) return;

    const nextPage = page + 1;

    searchNews(keyword, nextPage, pageSize);
  };

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