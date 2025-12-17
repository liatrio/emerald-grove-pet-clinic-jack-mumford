export type PetFormData = {
  name: string;
  birthDate: string; // yyyy-mm-dd
  type: string;
};

export function createPet(overrides: Partial<PetFormData> = {}): PetFormData {
  const suffix = `${Date.now()}`;

  return {
    name: `E2EPet${suffix}`,
    birthDate: '2018-01-01',
    type: 'cat',
    ...overrides
  };
}
