const LOCAL_BAY_KEY = "patchyLocalBay";

// Followed tech names for visitors without an account; merged into the
// account on sign-in so nothing is lost.
export const loadLocalBay = () => {
  try {
    const names = JSON.parse(localStorage.getItem(LOCAL_BAY_KEY) ?? "[]");
    return new Set(Array.isArray(names) ? names : []);
  } catch {
    return new Set();
  }
};

export const saveLocalBay = (names) => {
  localStorage.setItem(LOCAL_BAY_KEY, JSON.stringify([...names]));
};

export const clearLocalBay = () => {
  localStorage.removeItem(LOCAL_BAY_KEY);
};
