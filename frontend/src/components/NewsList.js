import React, { useState, useEffect } from "react";
import "./NewsList.css";

export default function NewsList({
  data,
  loading,
  isOffline,
  onLoadMore
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

  // Infinite Scroll
  useEffect(() => {

    const handleScroll = () => {

      if (
        window.innerHeight + document.documentElement.scrollTop + 200 >=
        document.documentElement.offsetHeight
      ) {
        if (!loading) {
          onLoadMore();
        }
      }
    };

    window.addEventListener("scroll", handleScroll);

    return () => window.removeEventListener("scroll", handleScroll);

  }, [loading]);

  const theme = {
    cardBg: darkMode ? "#111827" : "#ffffff",
    text: darkMode ? "#f8fafc" : "#0f172a",
    subText: darkMode ? "#94a3b8" : "#475569",
    heroBg: darkMode
      ? "linear-gradient(90deg,#0f172a,#1e293b)"
      : "linear-gradient(90deg,#2563eb,#4f46e5)"
  };

  return (
    <div style={{ minHeight: "100vh", color: theme.text }}>

      {isOffline && (
        <div style={{
          background: "#f59e0b",
          color: "white",
          padding: 8,
          textAlign: "center"
        }}>
          ⚠ Offline Mode – Showing cached results
        </div>
      )}

      {/* HERO */}
<div
  style={{
    background: theme.heroBg,
    color: "white",
    padding: "30px 20px",
    position: "relative",
    overflow: "hidden"
  }}
>

  <h1 className="hero-title">
    📰 Smart News Explorer
  </h1>

  {/* Floating particles */}
  <div className="hero-particles"></div>

  <div
    style={{
      display: "flex",
      justifyContent: "space-between",
      alignItems: "center",
      maxWidth: 1100,
      margin: "0 auto",
      flexWrap: "wrap",
      gap: 12,
      position: "relative",
      zIndex: 2
    }}
  >

    {/* API TIME BADGE */}
    {!loading && data?.timeTakenMs && (
      <div
        style={{
          fontSize: 13,
          background: "rgba(255,255,255,0.18)",
          padding: "7px 14px",
          borderRadius: 20,
          backdropFilter: "blur(8px)",
          fontWeight: 600,
          letterSpacing: 0.4,
          animation: "pulseBadge 2s infinite"
        }}
      >
        ⚡ {(data.timeTakenMs / 1000).toFixed(2)} sec
      </div>
    )}

    {/* DARK MODE BUTTON */}
    <button
      onClick={() => setDarkMode(!darkMode)}
      style={{
        padding: "7px 16px",
        borderRadius: 20,
        border: "none",
        cursor: "pointer",
        fontWeight: 600,
        fontSize: 13,
        background: darkMode
          ? "linear-gradient(135deg,#f8fafc,#e2e8f0)"
          : "linear-gradient(135deg,#1e293b,#0f172a)",
        color: darkMode ? "#0f172a" : "#ffffff",
        boxShadow: "0 6px 18px rgba(0,0,0,0.25)",
        transition: "all 0.35s ease"
      }}
    >
      {darkMode ? "☀ Light" : "🌙 Dark"}
    </button>

  </div>
</div>

      <div style={{
        maxWidth: 1200,
        margin: "40px auto",
        padding: "0 20px",
        display: "grid",
        gridTemplateColumns: "repeat(auto-fit,minmax(280px,1fr))",
        gap: 25
      }}>

        {articles.map((a, i) => (

          <div className="news-card"
            key={a.url || i}
            style={{
              backgroundColor: theme.cardBg,
              borderRadius: 16,
              overflow: "hidden",
              boxShadow: "0 8px 20px rgba(0,0,0,0.15)"
            }}
          >

            <img
              src={a.imageUrl || "https://placehold.co/600x400"}
              alt={a.title}
              style={{
                width: "100%",
                height: 200,
                objectFit: "cover"
              }}
            />

            <div style={{ padding: 16 }}>

              <h3>{a.title}</h3>

              {a.description && (
                <p style={{
                  fontSize: 13,
                  color: theme.subText
                }}>
                  {a.description.substring(0,120)}...
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
                  textDecoration: "none"
                }}
              >
                Read More →
              </a>

            </div>

          </div>
        ))}

      </div>

      {loading && (
        <div style={{ textAlign: "center", padding: 20 }}>
          Loading more news...
        </div>
      )}

    </div>
  );
}