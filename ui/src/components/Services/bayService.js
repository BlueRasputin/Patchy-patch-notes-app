
export const fetchBay = async (id) => {
  const response = await fetch(`http://localhost:8080/api/users/${id}/the-bay`);
  if (!response.ok) {
    throw new Error("Argh! Failed to catch from the bay");
  }
  return response.json();
};
// Fetches the user's bay(list of followed techs and patchnotes for each) from the backend