import React, { useState } from 'react';
import SearchForm from './components/SearchForm';
import NewsList from './components/NewsList';
import newsApi from './api/newsApi';

export default function App() {
  const [data, setData] = useState({});
  const [keyword, setKeyword] = useState('');      // optional local
  const [page, setPage] = useState(1);
  const [pageSize, setPageSize] = useState(10);
  const [offline, setOffline] = useState(false);

  const doSearch = async (queryData) => {
    // Decide effective params in this priority:
    // 1) explicit queryData (from SearchForm or pagination call)
    // 2) latest server-returned data (data.searchKeyword etc.)
    // 3) local state (keyword/page/pageSize/offline)
    const q = queryData?.q ?? data?.searchKeyword ?? keyword ?? 'latest';
    const p = queryData?.page ?? data?.page ?? page ?? 1;
    const ps = queryData?.pageSize ?? data?.pageSize ?? pageSize ?? 10;
    const off = queryData?.offline ?? (data?.offline ?? offline ?? false);

    // update local states if queryData provided (user typed new search)
    if (queryData) {
      setKeyword(q);
      setPage(p);
      setPageSize(ps);
      setOffline(off);
    }

    try {
      const resp = await newsApi.search(q, p, ps, off);
      setData(resp);
    } catch (err) {
      console.error('Search failed', err);
      // keep previous data or clear depending on UX
      setData({});
    }
  };

  // Called by NewsList when user clicks pagination
  const handlePageChange = async (newPage) => {
    // Build request from server-returned data first (most accurate),
    // but fallback to local state if data is missing
    const q = data?.searchKeyword ?? keyword ?? 'latest';
    const ps = data?.pageSize ?? pageSize ?? 10;
    const off = data?.offline ?? offline ?? false;

    // Update local page state
    setPage(newPage);

    // Use doSearch with explicit queryData to ensure request parameters are correct
    await doSearch({ q, page: newPage, pageSize: ps, offline: off });
  };

  return (
    <div
      style={{
        maxWidth: 1200,
        margin: '0 auto',
        padding: '40px 20px',
        fontFamily: 'Inter, system-ui, sans-serif',
      }}
    >
      <h1
        style={{
          textAlign: 'center',
          color: '#1e293b',
          marginBottom: 30,
          fontSize: '2rem',
          fontWeight: 600,
        }}
      >
        📰 News Aggregator
      </h1>

      <div
        style={{
          backgroundColor: '#f8fafc',
          borderRadius: 12,
          boxShadow: '0 2px 8px rgba(0,0,0,0.05)',
          padding: 10,
          marginBottom: 30,
        }}
      >
        {/* Pass a callback that triggers a new search when SearchForm submits */}
        <SearchForm onResults={setData} />
      </div>

      {data.articles ? (
        <NewsList data={data} onPageChange={handlePageChange} />
      ) : (
        <p style={{ textAlign: 'center', color: '#64748b' }}>
          No results yet. Try searching for something!
        </p>
      )}
    </div>
  );
}
