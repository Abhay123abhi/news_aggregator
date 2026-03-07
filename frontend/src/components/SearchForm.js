import React from "react";

export default function SearchForm({ onSearch }) {

  const [q, setQ] = React.useState("latest-news");
  const [pageSize, setPageSize] = React.useState(10);

  React.useEffect(() => {
    onSearch(q, 1, pageSize);
  }, []);

  function handleSubmit(e) {
    e.preventDefault();
    onSearch(q, 1, pageSize);
  }

  return (
    <div
      style={{
        display: "flex",
        justifyContent: "center",
        marginTop: 25,
        padding: "0 20px"
      }}
    >
      <form
        onSubmit={handleSubmit}
        style={{
          display: "flex",
          gap: 12,
          alignItems: "center",
          flexWrap: "wrap",
          background: "rgba(255,255,255,0.15)",
          backdropFilter: "blur(10px)",
          padding: "14px 18px",
          borderRadius: 14,
          boxShadow: "0 10px 30px rgba(0,0,0,0.15)"
        }}
      >
        <input
          value={q}
          onChange={(e) => setQ(e.target.value)}
          placeholder="🔎 Search news keyword..."
          style={inputStyle}
        />

        <input
          type="number"
          value={pageSize}
          min={1}
          step={1}
          onChange={(e) => setPageSize(Number(e.target.value))}
          style={{ ...inputStyle, width: 110 }}
          placeholder="Page Size"
        />

        <button style={buttonStyle}>
          🔍 Search
        </button>
      </form>
    </div>
  );
}

const inputStyle = {
  border: "1px solid #ccc",
  borderRadius: 6,
  padding: "6px 10px",
  minWidth: 150,
  fontSize: 14
};

const buttonStyle = {
  backgroundColor: "#2563eb",
  color: "#fff",
  border: "none",
  borderRadius: 6,
  padding: "6px 14px",
  cursor: "pointer"
};