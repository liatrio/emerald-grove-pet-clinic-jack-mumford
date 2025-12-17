import type { Page } from '@playwright/test';

import { BasePage } from './base-page';

export class VisitPage extends BasePage {
  constructor(page: Page) {
    super(page);
  }
}
