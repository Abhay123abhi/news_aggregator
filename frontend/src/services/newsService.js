export const getCacheKey = (keyword, page, pageSize) =>
  `news_${keyword}_${page}_${pageSize}`;

export const fetchNewsWithCache = async (keyword, page, pageSize) => {
  const cacheKey = getCacheKey(keyword, page, pageSize);

    try {
    const response = await fetch(
      `/api/news?q=${keyword}&page=${page}&pageSize=${pageSize}&offline=false`
    );

    if (!response.ok) {
      throw new Error("API failed");
    }

    const data = await response.json();

    localStorage.setItem(cacheKey, JSON.stringify(data));

    return { data, isOffline: false };
  } catch (error) {
    const cached = localStorage.getItem(cacheKey);

    if (cached) {
      return {
        data: JSON.parse(cached),
        isOffline: true,
      };
    }

    throw new Error("No cached data available");
  }
};