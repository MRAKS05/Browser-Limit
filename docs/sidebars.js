// @ts-check

/** @type {import('@docusaurus/plugin-content-docs').SidebarsConfig} */
const sidebars = {
  docs: [
    'index',
    'getting-started',
    {
      type: 'category',
      label: 'Guides',
      collapsed: false,
      items: [
        'detection',
        'gemini',
        'shizuku',
        'parental-control',
        'exceptions',
        'audit-log',
      ],
    },
    {
      type: 'category',
      label: 'Reference',
      collapsed: false,
      items: [
        'architecture',
        'api-reference',
        'permissions',
      ],
    },
    'faq',
    'troubleshooting',
  ],
};

module.exports = sidebars;
