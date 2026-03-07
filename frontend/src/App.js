import React, { useState } from "react";
import SearchForm from "./components/SearchForm";
import NewsList from "./components/NewsList";
import { fetchNewsWithCache } from "./services/newsService";

export default function App() {
  const [data, setData] = useState(null);
  const [loading, setLoading] = useState(false);
  const [isOffline, setIsOffline] = useState(false);
  const [timeTakenMs, setTimeTakenMs] = useState(null); // ⬅ store API time

  const handleSearch = async (keyword, page = 1, pageSize = 10) => {
    setLoading(true);
    setTimeTakenMs(null);

    try {
      const startTime = Date.now(); // ⬅ start timer

      const result = await fetchNewsWithCache(keyword, page, pageSize);

      const endTime = Date.now(); // ⬅ end timer
      setTimeTakenMs(endTime - startTime); // ⬅ calculate time taken

      setData({
        ...result.data,
        searchKeyword: keyword,
        page: page,
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
        timeTakenMs={timeTakenMs} // ⬅ pass it to NewsList
        onPageChange={handleSearch}
      />
    </div>
  );
}