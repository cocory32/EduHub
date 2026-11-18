/** @type {import('jest').Config} */
module.exports = {
  testEnvironment: 'jsdom',
  transform: {
    '^.+\\.tsx?$': ['ts-jest', { tsconfig: '<rootDir>/tsconfig.jest.json' }],
  },
  moduleNameMapper: {
    '^@eduhub/api-client$': '<rootDir>/../../packages/api-client/src/generated/index.ts',
  },
  setupFilesAfterEnv: ['<rootDir>/jest.setup.ts'],
};
