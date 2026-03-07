import React, { useState, useEffect } from "react";

export default function NewsList({
  data,
  loading,
  isOffline,
  onPageChange,
}) {
  const [darkMode, setDarkMode] = useState(false);

  const articles = data?.articles || [];

  useEffect(() => {
    document.body.style.margin = 0;
    document.body.style.transition = "background 0.4s ease";
    document.body.style.background = darkMode
      ? "linear-gradient(135deg, #0b1120, #020617)"
      : "linear-gradient(135deg, #f1f5f9, #e2e8f0)";
  }, [darkMode]);

  const handlePageChange = (newPage) => {
    if (newPage >= 1 && newPage <= data.totalPages) {
      onPageChange(data.searchKeyword, newPage);
    }
  };

  const theme = {
    cardBg: darkMode ? "#111827" : "#ffffff",
    text: darkMode ? "#f8fafc" : "#0f172a",
    subText: darkMode ? "#94a3b8" : "#475569",
    heroBg: darkMode
      ? "linear-gradient(90deg, #0f172a, #1e293b)"
      : "linear-gradient(90deg, #2563eb, #4f46e5)",
  };

  return (
    <div style={{ minHeight: "100vh", color: theme.text }}>

      {/* OFFLINE BANNER */}
      {isOffline && (
        <div
          style={{
            background: "#f59e0b",
            color: "white",
            padding: 8,
            textAlign: "center",
            fontSize: 13,
            fontWeight: 600,
          }}
        >
          ⚠ Offline Mode – Showing cached results
        </div>
      )}

      {/* HERO */}
      <div
        style={{
          background: theme.heroBg,
          color: "white",
          padding: "30px 15px",
          textAlign: "center",
        }}
      >
        <h1 style={{ marginBottom: 10 }}>
          📰 Smart News Explorer
        </h1>

        <button
          onClick={() => setDarkMode(!darkMode)}
          style={{
            padding: "6px 16px",
            borderRadius: 20,
            border: "none",
            cursor: "pointer",
            background: darkMode ? "#f8fafc" : "#0f172a",
            color: darkMode ? "#0f172a" : "#ffffff",
          }}
        >
          {darkMode ? "Light" : "Dark"} Mode
        </button>
      </div>

      {/* GRID */}
      <div
        style={{
          maxWidth: 1200,
          margin: "40px auto",
          padding: "0 20px",
          display: "grid",
          gridTemplateColumns: "repeat(auto-fit, minmax(280px, 1fr))",
          gap: 25,
        }}
      >
        {loading
          ? Array.from({ length: 8 }).map((_, i) => (
            <div
              key={i}
              style={{
                backgroundColor: theme.cardBg,
                borderRadius: 16,
                overflow: "hidden",
                boxShadow: darkMode
                  ? "0 8px 20px rgba(0,0,0,0.5)"
                  : "0 8px 20px rgba(0,0,0,0.1)",
              }}
            >
              {/* IMAGE */}
              <div className="skeleton skeleton-img" />

              <div style={{ padding: 16 }}>
                <div className="skeleton skeleton-title" />
                <div className="skeleton skeleton-text" />
                <div className="skeleton skeleton-text" />
                <div className="skeleton skeleton-text short" />
                <div className="skeleton skeleton-btn" />
              </div>
            </div>
          ))
          : articles.map((a, i) => (
            <div
              key={a.url || i}
              style={{
                backgroundColor: theme.cardBg,
                borderRadius: 16,
                overflow: "hidden",
                boxShadow: darkMode
                  ? "0 8px 20px rgba(0,0,0,0.5)"
                  : "0 8px 20px rgba(0,0,0,0.1)",
              }}
            >
              <img
                src={a.imageUrl || "https://placehold.co/600x400"}
                alt={a.title}
                style={{ width: "100%", height: 200, objectFit: "cover" }}
              />

              <div style={{ padding: 16 }}>
                <h3>{a.title}</h3>
                {a.description && (
                  <p style={{ fontSize: 13, color: theme.subText }}>
                    {a.description.substring(0, 120)}...
                  </p>
                )}

                <a
                  href={a.url}
                  target="_blank"
                  rel="noreferrer"
                  style={{
                    display: "inline-block",
                    marginTop: 10,
                    background: "#2563eb",
                    color: "white",
                    padding: "6px 12px",
                    borderRadius: 6,
                    textDecoration: "none",
                    fontSize: 13,
                  }}
                >
                  Read More →
                </a>
              </div>
            </div>
          ))}
      </div>

      {/* PAGINATION */}
      {!loading && data?.totalPages > 1 && (
        <div
          style={{
            display: "flex",
            justifyContent: "center",
            gap: 20,
            paddingBottom: 40,
          }}
        >
          <button
            onClick={() => handlePageChange(data.page - 1)}
            disabled={data.page <= 1}
          >
            ← Prev
          </button>

          <span>
            {data.page} / {data.totalPages}
          </span>

          <button
            onClick={() => handlePageChange(data.page + 1)}
            disabled={data.page >= data.totalPages}
          >
            Next →
          </button>
        </div>
      )}

      <style>
        {`@keyframes wave {
  0% {
    background-position: -400px 0;
  }
  100% {
    background-position: 400px 0;
  }
}

.skeleton {
  background: linear-gradient(
    90deg,
    #e5e7eb 25%,
    #f3f4f6 37%,
    #e5e7eb 63%
  );
  background-size: 800px 100%;
  animation: wave 1.6s infinite linear;
  border-radius: 6px;
}

.skeleton-img {
  width: 100%;
  height: 200px;
}

.skeleton-title {
  height: 20px;
  margin-bottom: 12px;
  width: 90%;
}

.skeleton-text {
  height: 12px;
  margin-bottom: 8px;
  width: 100%;
}

.skeleton-text.short {
  width: 70%;
}

.skeleton-btn {
  height: 28px;
  width: 100px;
  margin-top: 12px;
  border-radius: 6px;
}`}
      </style>
    </div>
  );
}