import { create } from 'zustand'

interface WalletState {
  userId: string;
  setUserId: (id: string) => void;
  // Other global states for UI
}

export const useWalletStore = create<WalletState>((set) => ({
  userId: '11111111-1111-1111-1111-111111111111', // Dummy default user ID
  setUserId: (id) => set({ userId: id }),
}))
