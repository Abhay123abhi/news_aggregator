import { useEffect, useState } from "react";

export default function SearchForm({ initialKeyword, initialPageSize, onSearch, loading }) {
  const [keyword, setKeyword] = useState(initialKeyword);
  const [pageSize, setPageSize] = useState(initialPageSize);
  useEffect(() => setKeyword(initialKeyword), [initialKeyword]);
  useEffect(() => setPageSize(initialPageSize), [initialPageSize]);
  function handleSubmit(event) { event.preventDefault(); const cleanKeyword = keyword.trim(); if (cleanKeyword) onSearch(cleanKeyword, pageSize); }
  return <form className="search-form" onSubmit={handleSubmit}>
    <label className="sr-only" htmlFor="news-search">Search news</label><span className="search-icon" aria-hidden="true">⌕</span>
    <input id="news-search" value={keyword} onChange={(event) => setKeyword(event.target.value)} maxLength="120" placeholder="Search a topic, place, or person" autoComplete="off" />
    <label className="sr-only" htmlFor="page-size">Stories per page</label><select id="page-size" value={pageSize} onChange={(event) => setPageSize(Number(event.target.value))}><option value={8}>8 stories</option><option value={12}>12 stories</option><option value={20}>20 stories</option></select>
    <button type="submit" disabled={loading || !keyword.trim()}>{loading ? "Searching…" : "Search"}</button>
  </form>;
}
