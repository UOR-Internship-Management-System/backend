# DESIGN.md — Student Portal UI Design Standard

## 1. Purpose

This document defines the UI design standard for the Student Portal HTML pages contained in the finalized project package. It must be used as the reference document before editing, fixing, or extending any page.

The design goal is to keep the portal visually consistent, accessible, responsive, and safe to maintain while preserving the existing page functionality.

This is not a functionality specification. It is a UI design and implementation guide.

---

## 2. Pages Covered

The current project contains these user-facing HTML pages:

| Page | Purpose | UI Category |
|---|---|---|
| `index.html` | Page launcher / navigation hub | Dashboard launcher |
| `01-student-sign-up.html` | Student registration | Auth page |
| `02-verify-otp.html` | OTP verification | Auth page |
| `03-create-new-password.html` | Password creation | Auth page |
| `04-student-login.html` | Student login | Auth page |
| `05-forgot-password.html` | Password recovery | Auth page |
| `06-student-profile.html` | Student profile, certificates, awards, activities, experience | Profile management page |
| `07-skills-final.html` | Skills entry, available skills, declared skills | Data-management page |
| `08-projects-final.html` | Project portfolio repository | Data-management page |
| `09-latex-cv-builder-final.html` | CV preview and LaTeX output | Workspace / builder page |
| `10-academic-records.html` | Academic records and GPA summary | Records / table page |

Supporting assets currently exist under:

```text
assets/css/design-system.css
assets/js/app.js
assets/js/pages/*.js
```

However, final HTML pages should not depend visually on `assets/css/design-system.css`. Each page may contain self-contained UI styling when standalone portability is required.

---

## 3. Core Design Direction

The UI uses a Material-inspired academic dashboard style.

Main characteristics:

- Google Sans typography
- Material Symbols icons
- Soft lavender page background
- White rounded cards in light mode
- Dark canvas cards in dark mode
- Purple primary accent
- Rounded inputs, buttons, chips, modals, and panels
- Soft shadows and subtle hover lift effects
- Generous internal spacing / breathing room
- Search, pagination, and action buttons kept inside the block they control
- Fully responsive layouts for desktop, tablet, and mobile

---

## 4. Non-Negotiable Safety Rules

When editing UI, never break functionality.

Do not remove, rename, or alter existing functional attributes unless explicitly required:

```text
id
name
onclick
onchange
onsubmit
oninput
href
src
action
method
type
value
data-*
aria-*
form IDs
input IDs
button IDs
modal IDs
table body IDs
pagination IDs
script references required for behavior
```

Allowed changes:

- Add UI-only wrapper elements
- Add UI-only classes
- Add styling rules
- Add visual helper text
- Add search and pagination controls where required
- Add demo/predefined data only when needed to test pagination/search visibility

Not allowed:

- Rewriting business logic unnecessarily
- Changing routing behavior
- Changing form validation behavior
- Changing payload field names
- Removing modal handlers
- Removing JavaScript files blindly
- Converting the project to another framework

---

## 5. Design Tokens

Use these tokens consistently across pages.

```css
:root {
    --primary: #6750A4;
    --on-primary: #FFFFFF;
    --surface-container: #F3EDF7;
    --surface-container-high: #ECE6F0;
    --canvas: #FFFFFF;
    --input-bg: #FDFBFF;
    --text: #1D1B20;
    --muted: #49454F;
    --outline: #79747E;
    --border: #E1DFFF;
    --danger: #B3261E;
    --danger-bg: #F9DEDC;
    --success: #00875A;
    --success-hover: #006644;

    --shadow-card: 0 4px 12px rgba(0,0,0,.05);
    --shadow-hover: 0 8px 22px rgba(103,80,164,.12);
    --shadow-modal: 0 18px 48px rgba(0,0,0,.22);

    --radius-xl: 24px;
    --radius-lg: 16px;
    --radius-md: 12px;
    --radius-pill: 999px;
    --radius-modal: 28px;

    --motion-standard: cubic-bezier(.4,0,.2,1);
    --motion-emphasized: cubic-bezier(.34,1.56,.64,1);
}
```

Dark mode tokens:

```css
body.dark-mode {
    --surface-container: #15121C;
    --surface-container-high: #24202D;
    --canvas: #211D29;
    --input-bg: #2B2633;
    --text: #F4EFF7;
    --muted: #CAC4D0;
    --outline: #948F99;
    --border: #494453;
    --danger-bg: #5F1B15;
    --success: #66D19E;
    --success-hover: #54B88A;

    --shadow-card: 0 6px 18px rgba(0,0,0,.35);
    --shadow-hover: 0 8px 24px rgba(0,0,0,.45);
    --shadow-modal: 0 18px 48px rgba(0,0,0,.55);
}
```

Material Web variable mapping should point to the same tokens:

```css
:root {
    --md-sys-color-primary: var(--primary);
    --md-sys-color-on-primary: var(--on-primary);
    --md-sys-color-surface-container: var(--surface-container);
    --md-sys-color-surface-container-high: var(--surface-container-high);
    --md-sys-color-on-surface: var(--text);
    --md-sys-color-outline: var(--outline);
    --md-ref-typeface-brand: 'Google Sans', sans-serif;
    --md-ref-typeface-plain: 'Google Sans', sans-serif;
}
```

---

## 6. Typography

Use Google Sans everywhere.

```css
* {
    box-sizing: border-box;
    margin: 0;
    padding: 0;
    font-family: 'Google Sans', sans-serif !important;
}

.material-symbols-outlined {
    font-family: 'Material Symbols Outlined' !important;
}
```

Heading scale:

| Element | Size | Weight | Usage |
|---|---:|---:|---|
| Page title | `1.85rem` | `700` | Main page heading |
| Section title | `1.3rem` | `700` | Card or block heading |
| Modal title | `1.5rem` | `700` | Modal headers |
| Card item title | `1.1rem` | `700` | Saved entries / rows |
| Body text | `.95rem` to `1rem` | `400/500` | Descriptions and metadata |
| Labels | `.85rem` | `700` | Form labels / small labels |

---

## 7. Page Layout Standard

Base page shell:

```css
body {
    background: var(--surface-container);
    color: var(--text);
    min-height: 100vh;
    padding: 40px;
    display: flex;
    justify-content: center;
    align-items: flex-start;
}

.main-container,
.container,
.profile-container,
.records-container,
.skills-container,
.workspace-enclosure,
.workspace-container {
    width: 100%;
    max-width: 1340px;
    margin: 0 auto;
    display: flex;
    flex-direction: column;
    gap: 32px;
}
```

Auth pages may use centered cards:

```css
body.auth-body,
body:has(.signup-container),
body:has(.signin-container),
body:has(.otp-card),
body:has(.password-card),
body:has(.recovery-container-card) {
    align-items: center;
    padding: 24px;
}
```

Responsive page padding:

```css
@media (max-width: 900px) {
    body {
        padding: 24px 14px;
    }
}
```

---

## 8. Page Header Pattern

Every main page should have a clear page header.

```html
<header class="page-header">
    <div>
        <h1>Page Title</h1>
        <p class="page-description">Short page purpose description.</p>
    </div>
    <button class="theme-toggle" id="themeToggle" type="button">
        <span class="material-symbols-outlined">dark_mode</span>
        <span>Dark Mode</span>
    </button>
</header>
```

Header CSS:

```css
.page-header {
    display: flex;
    justify-content: space-between;
    align-items: flex-start;
    gap: 24px;
}

.page-header h1 {
    font-size: 1.85rem;
    font-weight: 700;
    color: var(--text);
    line-height: 1.2;
    margin-bottom: 8px;
}

.page-header p {
    color: var(--muted);
    font-size: .95rem;
    line-height: 1.55;
    max-width: 840px;
}
```

---

## 9. Dark Mode Standard

Use `body.dark-mode` as the visible dark-mode state.

Theme toggle must:

- Toggle `body.dark-mode`
- Store the user choice using `studentPortalDarkMode`
- Update button text and icon
- Apply to every card, field, modal, table, pagination, chip, search bar, dropdown, and block

Dark mode must not leave any block in light styling. Pay special attention to:

- Work Experience block
- Certificate upload wrapper
- Search fields
- Dropdown labels
- Modal card windows
- Empty states
- Pagination buttons
- Saved entry cards
- Table headers
- Inline action buttons

If `assets/js/app.js` injects a floating `#themeToggleBtn`, hide it when the page has the header-level `#themeToggle` button:

```css
#themeToggleBtn {
    display: none !important;
}
```

---

## 10. Card and Panel Standard

All major blocks should use the same card language.

```css
.card-section,
.content-card,
.form-card,
.repository-card,
.profile-card,
.records-card,
.panel,
.profile-section-card,
.photo-upload-section,
.read-only-block,
.gpa-summary-card,
.results-section-card,
.work-experience-trigger-wrapper {
    background: var(--canvas);
    color: var(--text);
    border-radius: var(--radius-xl);
    padding: 32px;
    box-shadow: var(--shadow-card);
    border: 1px solid var(--border);
}
```

Internal card rhythm:

```css
.profile-section-card,
.repository-card,
.results-section-card,
.work-experience-trigger-wrapper {
    display: flex;
    flex-direction: column;
    gap: 24px;
}
```

Rule: a block’s search bar, list, pagination, and add button must live inside the same card as the content they control.

---

## 11. Search Bar Standard

Search bars must be placed inside the exact block they filter.

Correct pattern:

```text
Block header
Search bar / toolbar
List or table content
Pagination
```

Wrong pattern:

```text
Search bar in one block
Content being filtered in another block
```

Search wrapper:

```css
.search-wrapper,
.search-control-box,
.profile-search-toolbar {
    width: 100%;
    display: flex;
    align-items: center;
    justify-content: space-between;
    gap: 16px;
    margin-bottom: 18px;
}

.search-wrapper md-outlined-text-field,
.search-control-box md-outlined-text-field,
.profile-search-toolbar md-outlined-text-field {
    width: 100%;
    max-width: 520px;
}
```

Search behavior rules:

- Search must filter only the related block.
- Search must not affect other sections.
- Search should reset pagination to page 1.
- Search must work with predefined/demo data.
- Empty search results must show a clear empty-state message.
- Search bars must have bottom spacing before content.

Recommended labels:

| Block | Search Label |
|---|---|
| Certificates | `Search certificates` |
| Awards | `Search awards and achievements` |
| Activities | `Search extracurricular activities` |
| Experience | `Search professional experience` |
| Skills | `Search available system skills` |
| Projects | `Search saved projects` |
| Academic Records | `Search module, semester, or grade` |

---

## 12. Pagination Standard

Use one pagination style across Skills, Projects, Profile blocks, and Academic Records.

Preferred structure:

```html
<div class="pagination-bar" id="sectionPaginationBar">
    <div class="pagination-summary" id="sectionPaginationSummary"></div>
    <div class="pager" id="sectionPager"></div>
</div>
```

Pagination CSS:

```css
.pagination-bar,
.pagination-container,
.pagination-wrapper {
    display: flex;
    justify-content: space-between;
    align-items: center;
    gap: 16px;
    flex-wrap: wrap;
    margin-top: 18px;
}

.pagination-summary,
.page-summary,
.pagination-info {
    color: var(--muted);
    font-size: .88rem;
    font-weight: 500;
}

.pager,
.page-counter-group,
.pagination-controls {
    display: flex;
    align-items: center;
    gap: 8px;
    border: 0;
    background: transparent;
}

.pager button,
.page-index,
.pagination-btn {
    border: 1px solid var(--border);
    background: var(--canvas);
    color: var(--text);
    border-radius: 999px;
    min-width: 38px;
    height: 38px;
    padding: 0 12px;
    cursor: pointer;
    font-weight: 700;
}

.pager button.active,
.page-index.active,
.pagination-btn.active {
    background: var(--primary);
    color: #FFFFFF;
    border-color: var(--primary);
}

.pager button:disabled,
.pagination-btn:disabled {
    opacity: .45;
    cursor: not-allowed;
}
```

Pagination behavior rules:

- Pagination must be inside the same block as the data.
- Pagination must respect search results.
- Editing/removing items after filtering must target the original item index safely.
- Page count should update after add/edit/remove/delete.
- If a page becomes empty after deleting the last item on that page, move to the previous valid page.
- A demo page size of `2` is acceptable when predefined data is needed to visibly test pagination.

---

## 13. Button and Action Spacing

Button groups must never visually stick together.

```css
.inline-actions,
.row-actions-group,
.header-actions,
.modal-actions-wrapper,
.confirmation-actions-wrapper,
.actions-bar,
.button-row,
.form-actions,
.card-actions,
.section-actions,
.saved-entry-actions {
    display: flex;
    align-items: center;
    gap: 12px;
    flex-wrap: wrap;
}
```

Right-aligned groups:

```css
.modal-actions-wrapper,
.confirmation-actions-wrapper,
.actions-bar {
    justify-content: flex-end;
}
```

Mobile rule:

```css
@media (max-width: 900px) {
    .inline-actions,
    .saved-entry-actions,
    .modal-actions-wrapper,
    .confirmation-actions-wrapper,
    .actions-bar {
        width: 100%;
        justify-content: stretch;
        gap: 10px;
    }

    .inline-actions md-filled-button,
    .inline-actions md-outlined-button,
    .saved-entry-actions md-filled-button,
    .saved-entry-actions md-outlined-button,
    .modal-actions-wrapper md-filled-button,
    .modal-actions-wrapper md-outlined-button,
    .confirmation-actions-wrapper md-filled-button,
    .confirmation-actions-wrapper md-outlined-button {
        width: 100%;
    }
}
```

---

## 14. Form and Input Standard

Native input fallback:

```css
input,
select,
textarea {
    width: 100%;
    min-height: 48px;
    border: 1px solid var(--outline);
    border-radius: 12px;
    background: var(--canvas);
    color: var(--text);
    padding: 12px 16px;
    outline: none;
    font-size: 1rem;
    font-weight: 500;
}

input:focus,
select:focus,
textarea:focus {
    border-color: var(--primary);
    box-shadow: 0 0 0 4px rgba(103,80,164,.12);
}
```

Material field standard:

```css
md-outlined-text-field {
    width: 100%;
    --md-outlined-text-field-container-shape: 12px;
    --md-outlined-text-field-input-text-font: 'Google Sans', sans-serif;
    --md-outlined-text-field-label-text-font: 'Google Sans', sans-serif;
    --md-outlined-text-field-input-text-color: var(--text);
    --md-outlined-text-field-label-text-color: var(--muted);
    --md-outlined-text-field-supporting-text-color: var(--muted);
    --md-outlined-text-field-outline-color: var(--outline);
    --md-outlined-text-field-hover-outline-color: var(--primary);
    --md-outlined-text-field-focus-outline-color: var(--primary);
    --md-outlined-text-field-container-color: var(--canvas);
}
```

Form grid:

```css
.form-row,
.form-grid-layout,
.profile-grid,
.input-grid {
    display: grid;
    grid-template-columns: 1fr 1fr;
    gap: 24px;
}

.form-span-full {
    grid-column: span 2;
}

@media (max-width: 900px) {
    .form-row,
    .form-grid-layout,
    .profile-grid,
    .input-grid {
        grid-template-columns: 1fr;
    }

    .form-span-full {
        grid-column: span 1;
    }
}
```

Avoid inline `style="grid-column: span 2;"`; use `.form-span-full` instead.

---

## 15. Saved Entry Card Standard

Saved data rows should use card-like rows.

```css
.saved-entry-card,
.data-item-row,
.list-item-row,
.record-row {
    background: var(--input-bg);
    border: 1px solid var(--border);
    border-radius: 18px;
    padding: 20px 24px;
    color: var(--text);
    display: flex;
    justify-content: space-between;
    align-items: center;
    gap: 24px;
}

.saved-entry-content {
    flex: 1;
    min-width: 0;
    display: flex;
    flex-direction: column;
    gap: 6px;
}

.saved-entry-title {
    color: var(--text);
    font-size: 1.1rem;
    font-weight: 700;
    overflow-wrap: anywhere;
}

.saved-entry-meta,
.saved-entry-description,
.saved-entry-file {
    color: var(--muted);
    font-size: .95rem;
    line-height: 1.5;
    overflow-wrap: anywhere;
}
```

Mobile:

```css
@media (max-width: 900px) {
    .saved-entry-card {
        flex-direction: column;
        align-items: stretch;
    }
}
```

---

## 16. Chip / Badge Standard

Use chips for status, categories, skills, CV inclusion, and counts.

```css
.status-pill,
.cluster-chip,
.level-chip,
.project-chip,
.skill-chip,
.badge,
.token-chip,
.cv-status-pill {
    display: inline-flex;
    align-items: center;
    border-radius: 999px;
    background: color-mix(in srgb, var(--primary) 12%, var(--canvas));
    color: var(--primary);
    border: 1px solid color-mix(in srgb, var(--primary) 30%, transparent);
    padding: 5px 10px;
    font-size: .75rem;
    font-weight: 700;
    line-height: 1;
}
```

---

## 17. Table Standard

Tables must be wrapped in `.table-responsive`.

```css
.table-responsive {
    width: 100%;
    overflow-x: auto;
    border: 1px solid var(--border);
    border-radius: 18px;
}

table {
    width: 100%;
    border-collapse: collapse;
    min-width: 880px;
    table-layout: fixed;
}

th,
td {
    padding: 16px;
    border-bottom: 1px solid var(--border);
    vertical-align: middle;
    text-align: left;
}

th {
    background: color-mix(in srgb, var(--surface-container-high) 70%, var(--canvas));
    color: var(--text);
    font-size: .88rem;
    font-weight: 700;
}

td {
    color: var(--muted);
    font-size: .93rem;
    line-height: 1.4;
}
```

Mobile rule:

- If preserving a table, allow horizontal scroll.
- If converting to card rows, ensure each `td` has `data-label`.

---

## 18. Modal Standard

All modals should be centered, readable, and scroll-safe.

```css
.modal-overlay,
.app-modal-overlay {
    position: fixed;
    inset: 0;
    background: rgba(0,0,0,.38);
    display: none;
    justify-content: center;
    align-items: center;
    padding: 24px;
    z-index: 1000;
    opacity: 0;
    overflow-y: auto;
}

.modal-overlay.active,
.app-modal-overlay.active {
    display: flex;
    opacity: 1;
}

.modal-card,
.modal-window-card,
.modal-card-window,
.app-modal-card {
    width: min(880px, 100%);
    max-height: 82vh;
    background: var(--canvas);
    color: var(--text);
    border-radius: 28px;
    padding: 32px;
    box-shadow: var(--shadow-modal);
    border: 1px solid var(--border);
    display: flex;
    flex-direction: column;
    overflow: hidden;
}
```

Modal behavior rules:

- Modals must not take the full page height unnecessarily.
- Modal body should scroll internally if content is long.
- Header and footer actions should remain readable.
- Buttons must have a `gap`.
- Close buttons must be visible in dark mode.

---

## 19. Empty State Standard

```css
.empty-state,
.empty-state-message,
.empty-list-message {
    padding: 28px;
    text-align: center;
    color: var(--muted);
    border: 2px dashed var(--border);
    border-radius: 16px;
    background: var(--input-bg);
    font-style: italic;
}
```

Empty states must describe the exact block:

- `No certificates added yet.`
- `No awards or achievements match your search.`
- `No professional experience records match your search.`
- `No available skills found.`

---

## 20. Predefined Dataset Rule

Predefined data is acceptable when needed to demonstrate and test:

- Search
- Pagination
- Card spacing
- Edit/remove button alignment
- Empty search result states
- Dark mode with real content

Dataset requirements:

- At least 3 records per paginated block if page size is 2.
- Records should look realistic and academic.
- Demo data should not break hidden payload fields.
- Add/edit/remove must still work on demo data.
- Filtering must not corrupt original item indexes.

Profile page should include demo records for:

- Certificates
- Awards and Achievements
- Extracurricular Activities
- Professional Experience

Skills page should include enough system skill records to test search and pagination.

Projects page should include enough project records to test search, chips, pagination, details modal, edit, and remove.

Academic Records should include enough records to test table pagination and search.

---

## 21. Page-Specific Design Requirements

### 21.1 `index.html`

Purpose: navigation hub.

Requirements:

- Main centered container
- Page header with title, description, and theme toggle
- Card section for extracted pages
- Launcher cards with hover lift
- Clear labels and page numbers
- Navigation links must remain unchanged

---

### 21.2 `01-student-sign-up.html`

Purpose: registration.

Requirements:

- Split auth layout or centered auth card
- Strong left information panel if split layout is used
- Clear `Student Registration` form section
- Rounded inputs
- Submit button with correct spacing
- Theme toggle at top/right of form panel
- Mobile layout should stack cleanly

---

### 21.3 `02-verify-otp.html`

Purpose: identity verification.

Requirements:

- Centered OTP card
- Icon container at top
- OTP input group with adequate gap
- Resend link separated from submit action
- Dark mode text must remain readable

---

### 21.4 `03-create-new-password.html`

Purpose: password creation.

Requirements:

- Centered password card
- Clear helper text
- Password fields stacked with spacing
- Submit button full width if card is narrow
- Dark mode card and inputs styled

---

### 21.5 `04-student-login.html`

Purpose: authentication.

Requirements:

- Split or card-based layout matching signup page
- Forgot password link aligned cleanly
- Register link visually separated
- No cramped button/link spacing
- Mobile stack should not overflow

---

### 21.6 `05-forgot-password.html`

Purpose: password recovery.

Requirements:

- Centered recovery card
- Icon, title, description, email field, action button
- Back-to-login link with clear spacing
- Dark mode fully styled

---

### 21.7 `06-student-profile.html`

Purpose: student profile and CV-supporting records.

Critical requirements:

- Personal details fields should be visually grounded, preferably in a card.
- Left sidebar blocks must match dark mode.
- Certificates, Awards, Activities, and Professional Experience must each have:
  - Section header
  - Add button
  - Saved section title
  - Search bar inside the block
  - Saved list/cards
  - Pagination bar matching project/skills pagination style
- Search and pagination must work together.
- Predefined dataset must remain large enough to test pagination.
- Work Experience block must use the same card and dark-mode styling as other profile blocks.
- Button groups must have gaps.
- Save Profile button must not be too detached from the form.
- Modal forms must be scroll-safe.
- Delete confirmation modal must have spaced actions.

Preferred Profile block structure:

```html
<div class="profile-section-card">
    <div class="section-header-row">
        <div class="section-title">Certificates</div>
        <md-filled-button type="button">Add</md-filled-button>
    </div>

    <div class="section-entry-list">
        <div class="entry-list-title">Saved Certificates</div>
        <div class="profile-search-toolbar">...</div>
        <div class="dynamic-list-container" id="certificatesList"></div>
        <div class="pagination-bar" id="certificatesPaginationBar">
            <div class="pagination-summary" id="certificatesPaginationSummary"></div>
            <div class="pager" id="certificatesPager"></div>
        </div>
    </div>
</div>
```

---

### 21.8 `07-skills-final.html`

Purpose: skill declaration and skills inventory.

Requirements:

- Search bar should control Available System Skills.
- Dropdowns must be styled with direct UI theme.
- Dropdown labels must be visible in dark mode.
- Available skills must include enough dataset for pagination testing.
- Declared Skills table must align correctly.
- Table must be responsive.
- Remove action must use confirmation modal.
- Pagination style must match other pages.
- Add skill form and available skills block must have clear visual separation.

---

### 21.9 `08-projects-final.html`

Purpose: project portfolio repository.

Requirements:

- Repository card search bar belongs inside project repository block.
- Project count pill aligns with search/header controls.
- Project rows must show skill chips as capsule tags.
- Details modal must be vertically centered and not full-page height.
- Create/edit modal must be scroll-safe.
- Search and pagination must work together.
- Delete/remove should use confirmation modal.

---

### 21.10 `09-latex-cv-builder-final.html`

Purpose: CV preview and LaTeX output.

Requirements:

- Two-column workspace on desktop
- Single-column workspace on smaller screens
- CV preview should remain white to represent document paper
- Code panel should remain readable in dark mode
- Action buttons should have consistent spacing
- Update/Download/Submit buttons must be visually grouped but not cramped
- Modal must match same modal standard

---

### 21.11 `10-academic-records.html`

Purpose: academic records table.

Requirements:

- GPA summary cards at top
- Search bar inside records/results block
- Table inside `.table-responsive`
- Pagination using same project/skills/profile style
- Dark mode table header and rows must be readable
- Empty results state must appear if search finds nothing

---

## 22. Responsive Design Rules

General responsive baseline:

```css
@media (max-width: 900px) {
    body {
        padding: 24px 14px;
    }

    .page-header,
    .toolbar-row,
    .panel-header,
    .header-actions,
    .section-header-row,
    .work-experience-header-row {
        flex-direction: column;
        align-items: stretch;
    }

    .form-row,
    .form-grid-layout,
    .input-grid,
    .profile-grid,
    .dashboard-grid,
    .layout-grid,
    .workspace-grid {
        grid-template-columns: 1fr;
    }

    md-filled-button,
    md-outlined-button {
        width: 100%;
    }
}
```

Mobile priorities:

- No horizontal page overflow
- Buttons full width when stacked
- Modals should use `max-height: 88vh`
- Search bars full width
- Pagination centered or stacked
- Saved cards become vertical
- Tables scroll horizontally unless converted to card layout

---

## 23. Accessibility and UX Requirements

Minimum requirements:

- Every modal should use `role="dialog"` and `aria-modal="true"`
- Close buttons should have visible text or `aria-label`
- Search fields should have clear labels
- Buttons should preserve `type="button"` unless used for form submit
- Form submit buttons must remain `type="submit"`
- Color contrast must be checked in light and dark mode
- Keyboard navigation should remain possible
- Escape key may close the active modal if supported by global JS

---

## 24. JavaScript Safety Rules

When adding search and pagination:

- Keep original arrays/state names where possible.
- Preserve original item indexes when filtering.
- Do not use filtered indexes for edit/delete unless mapped to original indexes.
- Update hidden payload fields after add/edit/delete.
- Reset pagination to page 1 after search input changes.
- Clamp current page after delete.
- Re-render only the affected block.

Safe pattern:

```js
const rows = sourceArray
    .map((item, index) => ({ item, index }))
    .filter(({ item }) => matchesSearch(item));
```

Then use `index` for edit/delete actions.

---

## 25. Global App Script Consideration

`assets/js/app.js` currently provides useful global helpers such as:

- `showAppModal`
- `showConfirmModal`
- `paginationHtml`
- `bindPagination`
- `renderLoadingRows`
- `renderRealContent`
- Escape-key modal closing
- Link normalization

Do not remove `assets/js/app.js` unless the page has local replacements for every required helper.

If `app.js` injects a floating theme button, hide that floating button using CSS and keep the header theme toggle as the visible control.

---

## 26. UI Quality Checklist

Before finalizing any page, check:

### Layout

- [ ] Page has proper outer padding
- [ ] Main container max-width is consistent
- [ ] Page header is aligned
- [ ] Cards have consistent padding and radius
- [ ] No horizontal overflow

### Search

- [ ] Search bar is inside the correct block
- [ ] Search has bottom spacing
- [ ] Search filters only related content
- [ ] Search resets pagination
- [ ] Empty search state is shown

### Pagination

- [ ] Pagination appears inside the block
- [ ] Pagination style matches previous pages
- [ ] Pagination works after search
- [ ] Pagination works after add/edit/delete
- [ ] Summary text is accurate

### Buttons

- [ ] Button groups have visible gaps
- [ ] Mobile buttons stack cleanly
- [ ] Submit buttons keep correct type
- [ ] Action buttons do not touch card text

### Dark Mode

- [ ] Every card changes background
- [ ] Work Experience block changes background
- [ ] Search fields are readable
- [ ] Dropdown labels are readable
- [ ] Modals are readable
- [ ] Pagination buttons are readable
- [ ] Empty states are readable

### Modal UX

- [ ] Modal is centered
- [ ] Modal does not take full page height unnecessarily
- [ ] Modal content scrolls if needed
- [ ] Close button visible
- [ ] Actions have spacing

### Data

- [ ] Predefined demo data exists where needed
- [ ] Search can find demo data
- [ ] Pagination is visible using demo data
- [ ] Add/edit/remove still works
- [ ] Hidden payloads are updated

---

## 27. Definition of Done

A page is UI-complete only when:

1. It matches the direct standalone UI style.
2. It works in light mode and dark mode.
3. It is responsive on desktop, tablet, and mobile.
4. Search, pagination, and action buttons are inside the correct blocks.
5. Buttons and block content have enough breathing space.
6. The page has no visually disconnected controls.
7. Modals are centered and scroll-safe.
8. Existing functionality is preserved.
9. Page-specific JavaScript still works.
10. No required IDs, names, event handlers, or form fields were removed.

---

## 28. Preferred Future Maintenance Workflow

Before editing a page:

1. Read this `DESIGN.md`.
2. Identify the page category.
3. List functional selectors before editing.
4. Apply UI changes only.
5. Re-check functional selectors after editing.
6. Test light mode, dark mode, desktop, tablet, and mobile.
7. Test all modals, forms, search bars, pagination, and action buttons.
8. Only then package the final zip.

This file should be treated as the source of truth for all future UI updates.
