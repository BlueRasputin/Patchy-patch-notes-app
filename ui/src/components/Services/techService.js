
export const fetchTech = async () => {
  const response = await fetch("http://localhost:8080/api/tech");
  if (!response.ok) {
    throw new Error("Argh! Couldn't load tech");
  }
  return response.json();
};
//fetches the full tech  list from the backend


export const addTech = async (userId, techId) => {
  const response = await fetch(`http://localhost:8080/api/users/${userId}/favorites/${techId}`, {
    method: 'POST',
  });
  if (!response.ok) {
    throw new Error("Argh! Failed to add tech to favorites");
  }
  return response.json();
};
//adds a tech to the user's bay



export const removeTech = async (userId, techId) => {
  const response = await fetch(`http://localhost:8080/api/users/${userId}/favorites/${techId}`, {
    method: 'DELETE',
  });
  if (!response.ok) {
    throw new Error("Argh! Failed to remove tech from favorites");
  }
  return response.json();
}
//removes a tech from the user's bay