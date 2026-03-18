

export const fetchBay = async (userId) => {
  try {
    const response = await fetch(`http://localhost:8080/api/users/${userId}/patch-notes`, {
      method: "GET",
      headers: {
        "Content-Type": "application/json",
      },
      credentials: "include",
    });

    if (!response.ok) {
      throw new Error("Failed to get yer patch notes from The Bay, Matey!");
    }

    const data = await response.json();
    return data;
  } catch (error) {
    throw error;
  }
};
