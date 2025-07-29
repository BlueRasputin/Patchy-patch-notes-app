export const fetchUser = async (id) => {
  const response = await fetch(`http://localhost:8080/api/users/${id}`);
  if (!response.ok) {
    throw new Error("Argh! user not found");
  }
  return response.json();
};

export const addUser = async (userData) => {
  const response = await fetch("http://localhost:8080/api/users", {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json',
    },
    body: JSON.stringify(userData),
  });
  if (!response.ok) {
    throw new Error("Argh! Failed to add user");
  }
  return response.json("Ahoy! Welcome aboard, " + {username} + "!");
}
