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
        step={1}              // incremental
        onChange={(e) => setPage(Number(e.target.value))}
        style={{ ...inputStyle, width: 100 }}
        placeholder="Page"
      />


      {/* ✅ Page Size Added */}
      {/* Page Size */}
      <input
        type="number"
        value={pageSize}
        min={1}
        step={1}              // incremental
        onChange={(e) => setPageSize(Number(e.target.value))}
        style={{ ...inputStyle, width: 100 }}
        placeholder="Page Size"
      />

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