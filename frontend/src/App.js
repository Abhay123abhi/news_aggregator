import React, { useState } from "react";
import SearchForm from "./components/SearchForm";
import NewsList from "./components/NewsList";
import { fetchNewsWithCache } from "./services/newsService";

export default function App() {
  const [data, setData] = useState(null);
  const [loading, setLoading] = useState(false);
  const [isOffline, setIsOffline] = useState(false);

  const handleSearch = async (keyword, page = 1, pageSize = 10) => {
    setLoading(true);

    try {
      const result = await fetchNewsWithCache(
        keyword,
        page,
        pageSize
      );

      setData({
        ...result.data,
        searchKeyword: keyword,
        page: page,
        pageSize: pageSize,   // ✅ VERY IMPORTANT
      });

      setIsOffline(result.isOffline);

    } catch (err) {
      alert("No data available.");
    }

    setLoading(false);
  };

  return (
    <div>
      <SearchForm onSearch={handleSearch} />

      <NewsList
        data={data}
        loading={loading}
        isOffline={isOffline}
        onPageChange={handleSearch}
      />
    </div>
  );
}