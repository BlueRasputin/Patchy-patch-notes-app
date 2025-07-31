
export const fetchBay = async (userId) => {
  const response = await fetch(`http://localhost:8080/users/${userId}/the-bay`);
  if (!response.ok) {
    throw new Error("Argh! Failed to catch from the bay");
  }
  return response.json();
};
// Fetches the user's bay(list of followed techs and patchnotes for each) from the backend