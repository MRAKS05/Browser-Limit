// @ts-check

/** @type {import('@docusaurus/types').Config} */
const config = {
  title: 'Browser Limit',
  tagline: 'Automated browser detection and removal for Android. Enforce digital minimalism with AI-powered heuristics and rootless uninstallation.',
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

  markdown: {
    mermaid: true,
  },

  themes: ['@docusaurus/theme-mermaid'],

  plugins: [
    [
      require.resolve('@easyops-cn/docusaurus-search-local'),
      ({
        hashed: true,
        indexBlog: false,
        indexPages: false,
        docsRouteBasePath: '/',
        language: ['en'],
        highlightSearchTermsOnTargetPage: true,
        explicitSearchResultPath: true,
      }),
    ],
  ],

  presets: [
    [
      '@docusaurus/preset-classic',
      /** @type {import('@docusaurus/preset-classic').Options} */
      ({
        docs: {
          sidebarPath: require.resolve('./sidebars.js'),
          routeBasePath: '/',
          path: '.',
        },
        theme: {
          customCss: require.resolve('./src/css/custom.css'),
        },
        sitemap: {
          changefreq: 'weekly',
          priority: 0.5,
          ignorePatterns: ['/tags/**'],
        },
      }),
    ],
  ],

  themeConfig:
    /** @type {import('@docusaurus/preset-classic').ThemeConfig} */
    ({
      image: 'img/social-card.png',
      navbar: {
        title: 'Browser Limit',
        logo: {
          alt: 'Browser Limit Logo',
          src: 'img/logo.svg',
        },
        items: [
          {
            type: 'doc',
            docId: 'index',
            position: 'left',
            label: 'Docs',
          },
          {
            href: 'https://github.com/MRAKS05/Browser-Limit',
            label: 'GitHub',
            position: 'right',
          },
        ],
      },
      footer: {
        style: 'dark',
        links: [
          {
            title: 'Documentation',
            items: [
              {
                label: 'Getting Started',
                to: '/getting-started',
              },
              {
                label: 'Detection System',
                to: '/detection',
              },
              {
                label: 'Architecture',
                to: '/architecture',
              },
            ],
          },
          {
            title: 'Community',
            items: [
              {
                label: 'GitHub',
                href: 'https://github.com/MRAKS05/Browser-Limit',
              },
              {
                label: 'Report Issue',
                href: 'https://github.com/MRAKS05/Browser-Limit/issues',
              },
            ],
          },
          {
            title: 'More',
            items: [
              {
                label: 'License',
                href: 'https://github.com/MRAKS05/Browser-Limit/blob/main/LICENSE',
              },
            ],
          },
        ],
        copyright: `Copyright \u00a9 ${new Date().getFullYear()} Browser Limit. Built with Docusaurus.`,
      },
      prism: {
        theme: require('prism-react-renderer/themes/github'),
        darkTheme: require('prism-react-renderer/themes/dracula'),
        additionalLanguages: ['kotlin', 'bash', 'json', 'xml'],
      },
    }),
};

module.exports = config;
