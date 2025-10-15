
// export const fetchBay = async (userId) => {
//   const response = await fetch(`http://localhost:8080/users/${userId}/the-bay`);
//   if (!response.ok) {
//     throw new Error("Argh! Failed to catch from the bay");
//   }
//   return response.json();
// };
// // Fetches the user's bay(list of followed techs and patchnotes for each) from the backend

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
      throw new Error("Failed to fetch patch notes from The Bay");
    }

    const data = await response.json();
    return data;
  } catch (error) {
    console.error("Error fetching Bay:", error);
    throw error;
  }
};
