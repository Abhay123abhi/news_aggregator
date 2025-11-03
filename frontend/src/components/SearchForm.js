import React from 'react';
import newsApi from '../api/newsApi';

export default function SearchForm({ onResults }) {
  const [q, setQ] = React.useState('apple');
  const [page, setPage] = React.useState(1);
  const [pageSize, setPageSize] = React.useState(10);
  const [offline, setOffline] = React.useState(false);
  const [loading, setLoading] = React.useState(false);

  async function doSearch(e) {
    if (e) e.preventDefault();
    setLoading(true);
    try {
      const resp = await newsApi.search(q, page, pageSize, offline);
      onResults(resp);
    } catch (err) {
      alert('Failed to fetch: ' + err.message);
    }
    setLoading(false);
  }

  React.useEffect(() => { doSearch(); }, []);

  return (
    <form onSubmit={doSearch} style={{
      display: 'flex',
      flexWrap: 'wrap',
      gap: 8,
      alignItems: 'center',
      marginTop: 20,
      marginBottom: 20,
      justifyContent: 'center'
    }}>
      <input
        value={q}
        onChange={e => setQ(e.target.value)}
        placeholder="Keyword"
        style={inputStyle}
      />

      <input
        type="number"
        value={page}
        min={1}
        onChange={e => setPage(Number(e.target.value))}
        style={{ ...inputStyle, width: 80 }}
      />
      <input
        type="number"
        value={pageSize}
        min={1}
        onChange={e => setPageSize(Number(e.target.value))}
        style={{ ...inputStyle, width: 80 }}
      />
      <label style={{ fontSize: 13 }}>
        <input
          type="checkbox"
          checked={offline}
          onChange={e => setOffline(e.target.checked)}
        /> Offline
      </label>
      <button
        type="submit"
        disabled={loading}
        style={{
          backgroundColor: '#2563eb',
          color: '#fff',
          border: 'none',
          borderRadius: 6,
          padding: '6px 14px',
          cursor: 'pointer'
        }}
      >
        {loading ? 'Loading...' : 'Search'}
      </button>
    </form>
  );
}

const inputStyle = {
  border: '1px solid #ccc',
  borderRadius: 6,
  padding: '6px 10px',
  minWidth: 120,
  fontSize: 14
};
