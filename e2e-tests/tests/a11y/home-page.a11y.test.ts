import { test, expect } from '@playwright/test';
import { createRequire } from 'node:module';

type AxeImpact = 'minor' | 'moderate' | 'serious' | 'critical' | null;

interface AxeViolation {
  id: string;
  impact: AxeImpact;
  description: string;
  nodes: Array<{ target: string[] }>;
}

test('Home page accessibility scan (non-blocking)', async ({ page }) => {
  await page.goto('/');

  const require = createRequire(import.meta.url);
  const axePath = require.resolve('axe-core/axe.min.js');
  await page.addScriptTag({ path: axePath });

  const results = await page.evaluate(async () => {
    const w = window as any;
    return await w.axe.run(document, {
      runOnly: {
        type: 'tag',
        values: ['wcag2a', 'wcag2aa']
      }
    });
  });

  const violations = (results as { violations: AxeViolation[] }).violations;
  const critical = violations.filter((v) => v.impact === 'critical');
  const serious = violations.filter((v) => v.impact === 'serious');
  const debugMessage = violations
    .map((v) => `${v.impact ?? 'unknown'}: ${v.id} - ${v.description}`)
    .join('\n');

  if (critical.length > 0 || serious.length > 0) {
    test.info().annotations.push({
      type: 'a11y',
      description: `critical=${critical.length}, serious=${serious.length}`
    });
    console.warn(
      `Accessibility violations detected\ncritical=${critical.length}, serious=${serious.length}\n${debugMessage}`
    );
  }
});
