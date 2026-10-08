import { apiFetch } from "./api";
import { loadLocalBay } from "./localBay";

// Signed in: the account's Bay. Otherwise: the techs followed in this browser.
export const fetchBay = async (signedIn) => {
  if (!signedIn) {
    const response = await apiFetch("/api/patch-notes");
    if (!response.ok) {
      throw new Error("Failed to get yer patch notes from The Bay, Matey!");
    }
    const localBay = loadLocalBay();
    return (await response.json()).filter((note) => localBay.has(note.techName));
  }

  const response = await apiFetch("/api/me/patch-notes");

  if (!response.ok) {
    throw new Error(response.status === 401
      ? "Argh! Ye got to be logged in to view yer Bay!"
      : "Failed to get yer patch notes from The Bay, Matey!");
  }

  return response.json();
};
