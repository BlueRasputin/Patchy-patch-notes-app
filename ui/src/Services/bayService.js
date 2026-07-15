import { apiFetch } from "./api";

export const fetchBay = async (userId) => {
  const response = await apiFetch(`/api/users/${userId}/patch-notes`);

  if (!response.ok) {
    throw new Error("Failed to get yer patch notes from The Bay, Matey!");
  }

  return response.json();
};
