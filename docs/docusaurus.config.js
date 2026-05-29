// Docusaurus configuration for Browser-Limit docs
module.exports = {
  title: 'BrowserLimit Documentation',
  tagline: 'Comprehensive Guides and Reference for Safe App Usage',
  url: 'https://mraks05.github.io',
  baseUrl: '/Browser-Limit/',
  onBrokenLinks: 'throw',
  onBrokenMarkdownLinks: 'warn',
  favicon: 'img/favicon.ico',
  organizationName: 'MRAKS05',
  projectName: 'Browser-Limit',
  i18n: {
    defaultLocale: 'en',
    locales: ['en'],
  },
  presets: [
    [
      '@docusaurus/preset-classic',
      {
        docs: {
          sidebarPath: require.resolve('./sidebars.js'),
          routeBasePath: '/',
        },
        theme: {
          customCss: require.resolve('./src/css/custom.css'),
        },
      },
    ],
  ],
};
