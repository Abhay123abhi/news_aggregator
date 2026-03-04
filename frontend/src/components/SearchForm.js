import React from "react";

export default function SearchForm({ onSearch }) {
  const [q, setQ] = React.useState("latest-news");
  const [page, setPage] = React.useState(1);
  const [pageSize, setPageSize] = React.useState(10); // ✅ added

  function handleSubmit(e) {
    e.preventDefault();
    onSearch(q, page, pageSize); // ✅ just added pageSize
  }

  return (
    <form
      onSubmit={handleSubmit}
      style={{
        display: "flex",
        gap: 10,
        justifyContent: "center",
        marginTop: 20,
        flexWrap: "wrap",
      }}
    >
      <input
        value={q}
        onChange={(e) => setQ(e.target.value)}
        placeholder="Keyword"
        style={inputStyle}
      />

      <input
        type="number"
        value={page}
        min={1}
        onChange={(e) => setPage(Number(e.target.value))}
        style={{ ...inputStyle, width: 80 }}
      />

      {/* ✅ Page Size Added */}
      <select
        value={pageSize}
        onChange={(e) => setPageSize(Number(e.target.value))}
        style={{ ...inputStyle, width: 100 }}
      >
        <option value={5}>5</option>
        <option value={10}>10</option>
        <option value={20}>20</option>
        <option value={50}>50</option>
      </select>

      <button style={buttonStyle}>
        Search
      </button>
    </form>
  );
}

const inputStyle = {
  border: "1px solid #ccc",
  borderRadius: 6,
  padding: "6px 10px",
  minWidth: 120,
  fontSize: 14,
};

const buttonStyle = {
  backgroundColor: "#2563eb",
  color: "#fff",
  border: "none",
  borderRadius: 6,
  padding: "6px 14px",
  cursor: "pointer",
};