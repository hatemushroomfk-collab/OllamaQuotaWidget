# Acararchive HTML Post Guide v6.5

> Target: Arca.live posts edited through the Froala-based WYSIWYG editor.
> Intended users: GPT, Gemini, Claude, and other assistants given this guide together with project materials.
> Default content language: Korean. Instruction language may remain mixed for precision.
> Version history: `arca-guide-v6-post/CHANGELOG.md`.

---

## 0. Operating Contract

This document is a production specification, design vocabulary, and sanitizer-safe pattern library. It is not a closed template catalog.

### 0-1. Rule Levels

- **MUST / MUST NOT** — sanitizer survival, user-data preservation, explicit output requirements.
- **SHOULD / SHOULD NOT** — readability, mobile usability, accessibility, and coherent design.
- **MAY / EXPLORE** — visual variety, hybrid concepts, and experimental motifs.

Only sanitizer or data-safety rules use absolute bans. A common visual pattern is never banned merely because it is common.

### 0-2. Task Router

Classify the task before applying the interview protocol.

| Task | Interview required? | Expected behavior |
|---|---:|---|
| New full HTML build | Depends on authority mode | Resolve direction, then build |
| Existing HTML revision | Only for choices that materially change direction | Preserve unaffected content and structure |
| Aesthetic revision / `더 예쁘게` | Depends on authority mode | Preserve content and function; reopen the rejected visual direction |
| Review / comparison | No | Report findings; do not rebuild unless asked |
| Debugging / rendering diagnosis | No | Preserve the stated observation environment |
| Design ideation | No full interview | Offer concepts, not final HTML |
| Component or snippet request | Ask only blocking questions | Return the requested scope |
| Final paste-ready HTML | Direction must be resolved | Output HTML only |

Do not trigger a six-axis interview for review, debugging, planning, or comparison work.

### 0-3. Evidence and Preference Sources

Build a cumulative brief from all available sources:

1. the user's current explicit instruction;
2. choices confirmed in earlier turns;
3. supplied MD, JSON, HTML, images, exports, and other project materials;
4. clearly labeled model inference.

When values conflict, a newer explicit user instruction wins. Do not silently override an explicit choice with an attachment or inference.

Materials may establish content facts, available assets, palette sources, and likely information structure. They do not automatically prove a user preference when multiple plausible directions remain, unless design authority was delegated.

### 0-4. Design Authority Modes

Select one mode for a new build.

| Mode | Trigger | Action |
|---|---|---|
| **Direct Build** | Direction is sufficiently explicit | Build without presenting options |
| **Guided Choice** | User wants help choosing a direction | Present three compact concept cards and wait |
| **Targeted Interview** | One to three blocking choices are missing | Ask them together in one round |
| **Delegated Design** | User says `알아서`, `어울리게`, `적절히`, or clearly delegates design | Derive a coherent brief and proceed |

`알아서` is design authorization, not a failed answer. Do not force the user through concept selection after clear delegation.

For a bare build request with sufficient materials but no design direction or delegation, default to **Guided Choice**. Use **Targeted Interview** when the missing decision is factual or cannot be represented honestly as a design concept.

When the user's intent is genuinely unclear, ask one consolidated round of short Korean questions. Avoid serial one-question turns.

### 0-5. Cumulative Build Brief

Track these fields across the conversation. They do not all need to appear in one user message.

| Field | Typical source | Required before final build? |
|---|---|---:|
| Content type | User or materials | Yes |
| Content and asset inventory | Materials | Yes, within requested scope |
| Layout direction | User, selected concept, or delegated inference | Yes |
| Palette source | Images, logo, setting, or user | Yes |
| Brightness | User, selected concept, or delegated inference | Yes |
| Palette multiplicity | User, selected concept, or delegated inference | Yes |
| Detail level | User, content scope, or delegated inference | Yes |
| Density | User or reasonable inference | No; may be inferred |
| Originality preference | User | No; default `Balanced` |
| Visual ambition | User wording or reasonable inference | No; default `Designed` |
| Output mode | User or request wording | No; infer it when the deliverable is clear |
| Observation environment | User | Required only for rendering/debug tasks |

Do not repeatedly ask for a value already confirmed. If a user changes one field, update that field and keep unaffected decisions.

### 0-6. Originality Preference

- **Familiar** — prioritize established source-backed or pattern-derived baseline patterns.
- **Balanced** — use a safe structure with content-specific identity. Default when unspecified.
- **Experimental** — actively explore hybrid or custom structures, while respecting sanitizer rules.

Originality is not a sanitizer gate. An established compatible baseline remains valid in every mode when it fits the content or the user selects it.

### 0-7. Visual Ambition

Visual Ambition is independent from Originality and design authority.

- **Originality** controls how far the design departs from established structures.
- **Visual Ambition** controls how strongly the selected direction is staged.
- **Design authority** controls who selects that direction and whether the user waits for options.

A Familiar design may be Showcase, and an Experimental design may be Restrained. `Showcase` does not mean experimental, and `Delegated` does not mean visually restrained.

| Ambition | Typical signal | Staging expectation |
|---|---|---|
| **Restrained** | `깔끔하게`, `단정하게`, `절제해서` | Quiet hierarchy, controlled spacing, minimal visual peaks |
| **Designed** | `예쁘게`, `완성도 있게`, `보기 좋게`, or unspecified | Clear hierarchy, content-specific identity, at least one intentional focal scene |
| **Showcase** | `확실히 예쁘게`, `인상적으로`, `시선을 끌게`, `화려하게`, `강렬하게`, `임팩트 있게` | Memorable staging, deliberate impact rhythm, and a strong content-derived signature moment |

Showcase does not require vivid color, neon, multiple accents, or dense decoration. Visual force may come from scale, spacing, typography, material contrast, composition, or one memorable motif. A cream-paper, monochrome, or documentary design may be Showcase.

Do not ask for an ambition label when the wording or materials support a reasonable inference. Default to `Designed` when unspecified.

Route authority and ambition separately, then combine them:

| Request pattern | Route |
|---|---|
| `예쁘게` + direction already clear | Keep the current authority and strengthen that direction to Designed or the explicitly signaled level |
| `예쁘게` + direction unclear | Guided Choice; compare genuinely different visual directions |
| Existing result + `더 예쁘게` | Treat the current aesthetic as rejected; use Guided visual options when authority is not delegated, or internal candidates when it is delegated |
| `알아서 예쁘게` | Delegated Design + Showcase; compare internal candidates and build without waiting |
| `시안부터 예쁘게` | Guided Choice + Showcase; show compact visual samples and wait |

An aesthetic revision preserves content and functional requirements, not the rejected surface treatment. Do not return the same card stack wearing a louder palette.

### 0-8. Guided Choice Format

Offer exactly three meaningfully different directions unless the user requests a different count:

1. **Baseline** — established and implementation-safe;
2. **Alternative** — a different information structure or material;
3. **Original** — hybrid or content-derived custom concept.

Each concept card contains:

```text
Name:
Structure:
Palette source:
Tone: brightness / multiplicity / density
Scope: Minimal / Standard / Full
Visual ambition: Restrained / Designed / Showcase
Signature motif:
Why it fits:
Verification note: verified base / pattern-derived variation
```

Concepts SHOULD differ on at least four Design DNA axes from Section 3-2. A palette swap alone is not a different concept.

Use compact text concepts by default. Generate three small visual HTML samples when the user explicitly asks for `시안` or `후보`, or when an existing design was aesthetically rejected and the new direction remains unclear. Each sample contains only enough hero and one representative content block to compare direction; it is not a partial final post.

### 0-9. Delegated Design Procedure

When design authority is delegated:

1. extract content, assets, and constraints;
2. derive one or two content metaphors;
3. choose an information flow;
4. select or synthesize a Design DNA;
5. infer Visual Ambition separately from Originality;
6. compare three internal candidates using the compact record below;
7. prefer `Balanced` originality unless the request implies otherwise;
8. select the best-fitting candidate and build without asking non-blocking preference questions.

```text
Candidate:
Macro shape:
Hero move:
Material:
Palette roles: BG / SURFACE / TEXT / BORDER / ACCENT as concrete colors
Korean type stack:
Signature motif:
Impact rhythm:
Primary recipe:
Supporting recipe or none:
Fit note: one short sentence
Decision: select / reject
```

Candidates MUST differ on at least three of these axes: macro shape, hero move, material, typography mood, information flow, and signature motif. Three palette swaps are not three candidates. Keep this comparison internal in Delegated mode; expose compact samples only in Guided Choice.

Ask only when content, required files, URLs, or mutually exclusive product decisions are genuinely missing.

### 0-10. Output Modes

| Mode | Output |
|---|---|
| Concept options | Three compact text concepts; no full HTML |
| Draft | HTML may include placeholders; explanatory notes allowed outside the artifact |
| Revision | Changed HTML or file, plus concise change summary |
| Review | Findings and recommendations; no unauthorized rewrite |
| Snippet | Only the requested component |
| Final paste-ready | HTML only; no Markdown fence, preamble, design brief, or lint report |

For file-based work, commentary may accompany the saved artifact. `HTML only` governs the paste-ready artifact or direct final payload, not normal progress communication.

Do not ask `Draft or Final?` merely because the user did not name a mode. Infer **Final paste-ready** for an unqualified request for a complete usable HTML deliverable, and infer **Draft** when the user asks for a preview, experiment, placeholder build, or iterative direction check.

When **Revision** and **Final paste-ready** both apply, Final controls the artifact: return only the revised HTML in a direct paste-ready response. A concise change summary may accompany a saved file as separate handoff commentary, but MUST NOT be inserted into the HTML or appended to a direct HTML-only payload.

### 0-11. Observation Environment Lock

For rendering or debugging, preserve these user-provided fields exactly:

```text
관찰 환경:
대상 아님 환경:
재해석 금지:
```

Do not reinterpret a Claude/Gemini/GPT preview problem as an Arca sanitizer problem, or the reverse, without evidence.

---

## 1. Sanitizer and Editor Reality

### 1-1. Core Blocklist — MUST NOT Use in Live HTML

The following are treated as blocked, stripped, destructive, or unsafe for paste-ready Arca/Froala HTML.

```text
CSS layout: display:flex, display:grid, display:inline-grid, grid-template-*, position, z-index, overflow, gap
CSS effects: transform, animation, transition, opacity, filter, backdrop-filter
CSS clipping: clip-path, mask, -webkit-*
CSS interaction: cursor, pointer-events, user-select, resize
CSS other: writing-mode, content, conic-gradient, mix-blend-mode, isolation, outline, outline-offset
CSS values: url() in any live CSS value
Selectors/rules: ::before, ::after, :hover, :nth-child, @media, @keyframes
Tags: style, script, link, code, button, form, input, select, textarea
Tags: mark, small, big, kbd, samp, var, tt, audio, object, picture, source, svg, math
Tags: template, noscript, dl, dt, dd, abbr, cite, dfn, q, data, bdi, bdo, wbr
Syntax: HTML comments, event handlers, javascript: URLs, custom classes as styling hooks
Images: data: URI images
```

`display:inline-flex` is distinct from `display:flex` and may be used for compact inline groups. Do not silently normalize it to flex.

When transparency is needed, use an `rgba()` color channel. Do not emulate it with the blocked `opacity` property.

### 1-2. Core Allowed Vocabulary

Use inline `style=""` declarations. The following are the core working vocabulary. **Allowed does not automatically mean verified**: a specific `verified` claim still requires an evidence ID under Section 12-5.

```text
Tags: strong, em, u, s, del, ins, sub, sup, pre, a, img, video, iframe
Tags: span, div, br, hr, p, h1-h6, blockquote, details, summary
Tags: ul, ol, li, table, thead, tbody, tfoot, tr, th, td, ruby, rt, time

Color: color, background, background-color, linear-gradient(), radial-gradient(), rgba()
Type: font-size, font-weight, font-family, font-style, font-variant
Type: letter-spacing, word-spacing, line-height, text-align, text-indent
Type: text-decoration, text-transform, vertical-align
Box: padding, margin, border, border-radius, border-collapse, border-spacing
Box: border-image, width, max-width, min-width, height, min-height
Layout: display:table, display:table-cell, display:inline-block, display:inline-flex
Layout: float:right inside summary, list-style:none
Effects: box-shadow, text-shadow
```

`calc()` and `clamp()` are retained as pattern-derived capabilities until a dated verification source is attached. Do not make a layout depend on them when a simple fixed or percentage value works.

The supplied reference file reports that CSS `var()` can pass sanitization, but its saved live styles do not prove a usable custom-property declaration-and-reference pair. Treat CSS `var()` as **sanitizer-pass reported but operationally unverified** and never make a final layout depend on it. This is separate from the blocked HTML `<var>` tag.

### 1-3. Editor Transformations

| Behavior | Safe response |
|---|---|
| Empty `span`/`div` may be removed | Insert `&nbsp;` when the element is decorative |
| Empty screenshot `td` is intentionally used as an editor insertion slot | Keep it empty; this is the explicit exception to the empty-element rule |
| `b`, `i`, `strike` may be normalized | Prefer `strong`, `em`, `s` |
| `blockquote` style may be stripped | Style an inner `div` or `span` |
| `a` (anchor) inline style may be stripped on save | Put button background, padding, and radius on a child `inline-block` `span`/`div`; keep only `href`, `target`, `rel`, and `text-decoration` on the `<a>` |
| Image source/classes may be rewritten | Expect CDN rewriting; manually written images use the editor-compatible class when verified |
| Table HTML attributes may be stripped | Put borders and spacing in inline styles |
| Bare table defaults may appear | Declare table and cell borders explicitly |
| Flat backgrounds or dark neutral text may be theme-overridden | Use tested gradient/colored-ink recipes and verify both themes |

Parent backgrounds visually sit behind transparent children, but editor/theme overrides may still affect child blocks. Set explicit child backgrounds only where the design depends on a specific surface; do not duplicate them blindly on every element.

Previously observed editor-owned image classes such as `fr-fic fr-dii` are not custom styling hooks. Preserve them when revising editor-produced HTML. For a manually authored image, verify the class and post-save result in the current target rather than assuming a chat preview is equivalent.

### 1-4. Safe Structural Patterns

- Horizontal layout: `display:table` with `display:table-cell` children.
- Card grid: layout table with `border-collapse:separate`, explicit `border-spacing`, table `border:0`, and explicit cell borders; follow Section 8-1 accessibility limits.
- Collapsible content: `details` and `summary`.
- CTA: anchor wrapping a styled `inline-block` child; keep the button surface off the anchor and never use `button`.
- Code presentation: styled `pre` or monospace `span`; never `code`.
- Blockquote: style an inner wrapper.
- Screenshot insertion: caption row followed by an intentionally empty image cell; treat the height behavior as experimental until save/reload verification.
- Decorative empty shape: explicit dimensions plus `&nbsp;`.
- Indented displayed code: `br` and `&nbsp;` when editor preservation matters.

### 1-5. Displayed Code vs Live Styling

Blocklisted strings shown as reader-facing code are text, not active CSS. A displayed example may contain the words `display:flex` inside `pre`; live `style="display:flex"` remains prohibited.

Displayed code SHOULD be visually distinguishable with monospace type and a dedicated surface. Mechanical lint must inspect tags and style attributes rather than blindly rejecting every blocked word in text content.

### 1-6. Verification Status

Every new technique or skeleton carries one status:

- **verified** — traced to a specific source that survived the target environment;
- **pattern-derived** — composed only from allowed vocabulary but not directly field-tested as a whole;
- **experimental** — uncertain behavior or a deliberate compatibility probe;
- **deprecated** — retained only to explain why it should no longer be used.

`pattern-derived` is production-eligible and may be selected normally. Verification status controls evidence claims and testing obligations, not visual priority. A pattern-derived design is not lower priority than a verified baseline. Only `experimental` requires general compatibility caution. Specific local cautions attached to an individual pattern still apply.

Do not broaden a verified claim. Verification of two inset shadows does not verify every multi-layer background or arbitrary layer count.

---

## 2. Build Scope and Complexity

Complexity controls content coverage, not mandatory decoration counts.

| Level | Typical size | Design expectation |
|---|---|---|
| Minimal | About one screen | One clear hierarchy; callouts and meta optional |
| Standard | Two to three screens | One coherent family with controlled rhythm changes |
| Full | Four or more screens | One coherent family, richer information flow, two or three supporting motifs |

Do not force a callout, meta strip, or four header types merely to satisfy a quota. Add components when the content needs them.

Within a document, repeated sections SHOULD vary when repetition harms scanning, but repeated forms remain valid when they communicate a repeated content type. Coherence outranks novelty after a direction is selected.

---

## 3. Design Generation System

### 3-1. Content-First Sequence

Do not start by selecting a named design from the registry.

1. Distill the content into one short essence statement.
2. Derive one or two metaphors from the supplied world, product, or material.
3. Decide the information flow.
4. Define the Design DNA.
5. Compare it with the registry for vocabulary and safe anchor selection.
6. Use an existing, hybrid, or custom direction.

The registry is an inspiration and routing index, not a closed menu.

### 3-2. Design DNA

| Axis | Examples |
|---|---|
| Macro shape | single column, split, catalog, stacked log, cover-and-body |
| Information flow | editorial narrative, procedural, chronological, comparative, collectible |
| Header identity | masthead, accession label, chapter title, status bar, ticket stub |
| Material | flat UI, paper, parchment, cork, terminal glass, metal, broadcast panel |
| Density rhythm | dense, balanced, airy, alternating |
| Typography mood | modern sans, bureaucratic mono, literary serif, display-heavy |
| Signature motif | serial number, frequency, specimen code, quest state, route label |

Design DNA describes and stabilizes a choice. It does not require the result to differ from every existing anchor.

### 3-3. Scene Plan and Purposeful Variation

Before a Standard or Full build, assign an impact level to the major information scenes. This is a staging plan, not a decoration quota.

Typical major scenes include:

- Hero or opening identity;
- orientation or premise;
- core value, primary features, or central reveal;
- usage, process, or examples;
- caution, compatibility, or verification;
- download, release, or closing action.

Example:

```text
Scene plan:
1. Hero — loud
2. Orientation — calm
3. Core value — medium
4. Detail folds — calm
5. Download finale — medium or loud
```

The exact rhythm follows Visual Ambition. Restrained may remain calm with one clear medium focus. Designed SHOULD contain an intentional focal scene. Showcase SHOULD contain at least one memorable peak, but the peak may come from whitespace, scale, typography, material, or composition rather than saturated color.

When consecutive major scenes serve different information roles, distinguish them purposefully through one or more meaningful axes: header role, material, width, density, alignment, or impact. Use two axes when a stronger transition improves scanning; do not change axes merely to satisfy a count.

Repeated structures are exempt when repetition communicates sameness. Examples include feature cards of one type, step lists, setting rows, repeated profiles, and same-level collapsible items.

Use three header roles when the content hierarchy needs them:

- **Hero title** — document identity and first impression;
- **Major section title** — primary navigation through the content;
- **Compact subsection label** — local grouping and metadata.

Different colors do not create different header roles when size, placement, and information function remain the same.

### 3-4. Safe Baseline Protection

Established compatible baseline patterns are always valid regardless of whether their exact composition is verified or pattern-derived. Centered cards, gradient headers, magazine strips, dossiers, terminals, and other established structures must not be rejected merely because they are common.

Use a baseline when the user selects it, when it fits the content, when reliability is important, or when a familiar presentation is desirable. Diversity rules expand available choices; they do not invalidate baseline designs.

If originality was explicitly requested, MAY compare the draft with the nearest reference and introduce one meaningful content-specific variation. If originality was not requested, no structural novelty is mandatory.

When Visual Ambition is Showcase, a safe baseline may remain the foundation but is not the entire staging plan. Add at least one content-derived signature scene or impact move without banning or disguising the baseline.

### 3-5. Cohesion and Local Variety

- Keep one dominant design family per post.
- Use no more than two or three supporting motifs in most Full posts.
- Consecutive major scenes SHOULD differ purposefully when their information roles differ; Section 3-3 exemptions apply.
- Repeated data rows SHOULD retain the same form.
- A palette micro-adjustment is not structural variety.
- Never degrade readability merely to prove originality.

---

## 4. Compact Design Registry

The registry describes deltas from the eight anchors in Section 5. Entries are not complete templates.

### 4-1. Publication

| ID | Direction | Anchor | Signature delta | Best for |
|---|---|---|---|---|
| PUB-01 | Newspaper / Broadsheet | A1 | Masthead, issue/date row, ruled columns | Patch notes, long reports |
| PUB-02 | Magazine Feature | A1 | Cover title, meta strip, pull quote | Major introductions |
| PUB-03 | Independent Zine | A1 | Fragmented labels, bold type, mixed rule weight | Personal or experimental work |
| PUB-04 | Cinema Credits | A1 | Centered sequences, scene chapters, wide breathing room | Emotional narrative intros |

### 4-2. Archive and Institutional

| ID | Direction | Anchor | Signature delta | Best for |
|---|---|---|---|---|
| ARC-01 | Classified Dossier | A3 | Clearance labels, file codes, redactions | Secret lore, SCP-style material |
| ARC-02 | Museum Archive | A2 | Accession number, curator note, provenance | Characters, artifacts, asset packs |
| ARC-03 | Medical Chart | A3 | Patient ID, findings, diagnosis rows | Clinical or horror concepts |
| ARC-04 | Specimen File | A2 | Taxonomy, condition, observation notes | Creatures, items, worldbuilding |

### 4-3. Technical and System

| ID | Direction | Anchor | Signature delta | Best for |
|---|---|---|---|---|
| TEC-01 | Terminal / Console | A4 | Prompt lines, timestamps, phosphor identity | Logs, code, technical fiction |
| TEC-02 | Blueprint Sheet | A8 | Coordinate labels, cyan rules, process blocks | Architecture and module explanations |
| TEC-03 | Control Panel | A8 | Status lamps, metric cells, operation groups | Utilities and toggles |
| TEC-04 | Operating System Window | A8 | Title bars, file rows, system notices | Extensions and software intros |

### 4-4. Broadcast and Information

| ID | Direction | Anchor | Signature delta | Best for |
|---|---|---|---|---|
| BRD-01 | News Broadcast | A7 | Channel ID, headline band, lower-third rows | Announcements and summaries |
| BRD-02 | Emergency Bulletin | A7 | Severity band, action list, timestamp | Breaking changes and warnings |
| BRD-03 | Radio Intercept | A4 | Frequency, signal state, transcript log | Mystery, military, sci-fi |
| BRD-04 | Weather / Status Board | A7 | Region cells, condition labels, forecast strip | Multi-state summaries |

### 4-5. Commercial and Distribution

| ID | Direction | Anchor | Signature delta | Best for |
|---|---|---|---|---|
| COM-01 | Product Catalog | A2 | Category cards, SKU/version, feature rows | Asset packs and multi-item releases |
| COM-02 | Auction Listing | A2 | Lot number, provenance, bid/status block | Relics, characters, collectible concepts |
| COM-03 | Ticket / Boarding Pass | A6 | Serial, perforation-like dashed split, stub CTA | Downloads and events |
| COM-04 | Classified Advertisement | A1 | Compact offer, price/version, contact CTA | Small releases and playful intros |

### 4-6. Narrative and Log

| ID | Direction | Anchor | Signature delta | Best for |
|---|---|---|---|---|
| NAR-01 | Field Journal | A5 | Date/place headings, observation entries | Exploration and character diaries |
| NAR-02 | Mythic Manuscript | A5 | Chapter ornaments, relic terminology, framed excerpts | Fantasy and legend |
| NAR-03 | Expedition Log | A4 | Day markers, coordinates, supply/status rows | Journey or survival narratives |
| NAR-04 | Case Notes | A3 | Evidence numbers, interview extracts, conclusions | Mystery and investigative content |

### 4-7. Game and Progress

| ID | Direction | Anchor | Signature delta | Best for |
|---|---|---|---|---|
| GAM-01 | Quest Log | A3 | Objective state, reward, expandable steps | Interactive scenarios and guides |
| GAM-02 | Character Select | A2 | Roster cards, role tags, selected profile | Multi-character bots |
| GAM-03 | Achievement Board | A2 | Badge state, requirement, completion note | Feature and milestone lists |
| GAM-04 | Crafting Recipe | A8 | Ingredient/result split, numbered process | Tutorials and combination systems |

### 4-8. Material and Craft

| ID | Direction | Anchor | Signature delta | Best for |
|---|---|---|---|---|
| MAT-01 | Pinboard | A2 | Cork wrapper, pinned cream notes | Casual rosters and shared assets |
| MAT-02 | Scrapbook | A5 | Paper layers, caption strips, keepsake rhythm | Memories and slice-of-life themes |
| MAT-03 | Parchment Codex | A5 | Warm dark paper, inset depth, chapter marks | Mythic or historical content |
| MAT-04 | Metal Plaque | A8 | Sharp borders, engraved text-shadow, serial plate | Industrial and military themes |

### 4-9. Social and Communication

| ID | Direction | Anchor | Signature delta | Best for |
|---|---|---|---|---|
| SOC-01 | Chat Log | A4 | Speaker labels, timestamps, alternating messages | Dialogue and samples |
| SOC-02 | Social Feed | A2 | Profile meta, post cards, reaction summary | Modern character presentation |
| SOC-03 | Email Thread | A3 | From/to/subject fields, quoted replies | Technical or narrative exchanges |
| SOC-04 | Community Profile | A2 | Handle, badges, activity and link blocks | Creator or character profiles |

### 4-10. Ceremonial and Fantasy

| ID | Direction | Anchor | Signature delta | Best for |
|---|---|---|---|---|
| CER-01 | Tarot Record | A5 | Arcana number, upright/reversed meanings | Symbolic characters and lore |
| CER-02 | Relic Catalog | A2 | Rarity, origin, effect, warning | Items and fantasy assets |
| CER-03 | Theater Program | A1 | Act list, cast, intermission dividers | Ensemble casts and narrative modules |
| CER-04 | Invitation / Proclamation | A5 | Addressee, occasion, seal-like motif | Events and formal releases |

### 4-11. Custom and Hybrid Directions

The registry is not exhaustive. A custom direction is valid when its Design DNA can be implemented with Section 1 vocabulary. Name hybrids by the content metaphor rather than merely joining two registry labels.

Examples:

```text
After-Hours Attendance Ledger = ARC-04 structure + NAR-01 rhythm + school record motif
Hadal Observation Broadcast = BRD-01 structure + TEC-02 rules + depth/frequency motif
Dream Auction Catalogue = COM-02 structure + CER-01 typography + memory-lot numbering
```

These are concept formulas, not verified finished skeletons.

---

## 5. Anchor Skeleton Library

The eight anchors are sanitizer-aware structures, not mandatory visual templates. Replace tokens from Section 7, adapt content order, and keep the selected Design DNA coherent.

Unreplaced `{TOKENS}` are placeholders and MUST NOT remain in final paste-ready HTML.

### 5-1. A1 — Editorial / Publication

**Status:** pattern-derived composition. Its gradient, border, and inline-style primitives are source-backed where listed in [EV-ARCA-ALLOW-001], but the complete anchor has not been field-verified as a unit.

```html
<div style="max-width:900px;margin:0 auto;background:linear-gradient(160deg,{BG},{SURFACE},{BG});color:{TEXT};font-family:{FONT_BODY};border:1px solid {BORDER};">
  <div style="padding:36px 28px 30px;text-align:center;border-bottom:3px double {BORDER};">
    <div style="font-size:11px;letter-spacing:5px;color:{META};font-weight:700;">ISSUE · DATE · CATEGORY</div>
    <div style="font-size:34px;line-height:1.2;font-weight:900;color:{HEADING};margin-top:12px;">Publication Title</div>
    <div style="font-size:14px;line-height:1.8;color:{TEXT};max-width:620px;margin:14px auto 0;">Short deck or opening statement.</div>
  </div>
  <div style="padding:28px;line-height:1.9;font-size:14px;background:linear-gradient(180deg,{SURFACE},{BG});">
    Main editorial content
  </div>
</div>
```

Useful deltas: masthead rules, issue metadata, two-column table-cell body, centered cinema-credit sequence, or bold zine labels.

### 5-2. A2 — Catalog / Card Grid

**Status:** pattern-derived pending an evidence record for the complete card grid.

```html
<div style="max-width:900px;margin:0 auto;padding:22px;background:linear-gradient(160deg,{BG},{SURFACE});color:{TEXT};font-family:{FONT_BODY};">
  <div style="font-size:24px;font-weight:900;color:{HEADING};margin-bottom:16px;">Catalog Title</div>
  <table style="width:100%;border-collapse:separate;border-spacing:14px 0;border:0;">
    <tbody>
      <tr>
        <td style="width:50%;vertical-align:top;padding:18px;background:linear-gradient(160deg,{CARD_A},{SURFACE});border:1px solid {BORDER_A};">
          <div style="font-size:16px;font-weight:800;color:{HEADING};">Item A</div>
          <div style="font-size:13px;line-height:1.75;color:{TEXT};margin-top:8px;">Description</div>
        </td>
        <td style="width:50%;vertical-align:top;padding:18px;background:linear-gradient(160deg,{CARD_B},{SURFACE});border:1px solid {BORDER_B};">
          <div style="font-size:16px;font-weight:800;color:{HEADING};">Item B</div>
          <div style="font-size:13px;line-height:1.75;color:{TEXT};margin-top:8px;">Description</div>
        </td>
      </tr>
    </tbody>
  </table>
</div>
```

Each cell MUST declare its own border. Do not replace the cell border with an inset shadow. On mobile, use two text cards per row at most.

For a 2×2 catalog, prefer two separate one-row tables instead of adding complex spacer cells or nested grids. Use three-column rows only for compact metadata or very short catalog labels.

### 5-3. A3 — Dossier / Archive

**Status:** pattern-derived composition. Do not promote the full dossier or its fixed split from primitive-level evidence.

```html
<div style="max-width:900px;margin:0 auto;background:linear-gradient(160deg,{BG},{SURFACE});border:2px solid {BORDER};color:{TEXT};font-family:{FONT_BODY};">
  <div style="display:table;width:100%;border-bottom:1px solid {BORDER};">
    <div style="display:table-cell;width:68%;vertical-align:middle;padding:20px 24px;">
      <div style="font-size:10px;letter-spacing:4px;color:{META};font-family:{FONT_MONO};">FILE / {SERIAL}</div>
      <div style="font-size:25px;font-weight:900;color:{HEADING};margin-top:6px;">Record Title</div>
    </div>
    <div style="display:table-cell;width:32%;vertical-align:middle;padding:20px;text-align:right;border-left:1px solid {BORDER};">
      <span style="display:inline-block;padding:5px 9px;border:1px solid {ACCENT};color:{ACCENT};font-size:10px;font-weight:800;letter-spacing:2px;">STATUS</span>
    </div>
  </div>
  <div style="padding:22px 24px;font-size:14px;line-height:1.85;background:linear-gradient(180deg,{SURFACE},{BG});">Record body</div>
</div>
```

Specialize through field names and information flow rather than defaulting every archive to red `CLASSIFIED` labels.

At roughly 360px, use a vertical dossier header when the title, serial, or status label is long. Keep the 68/32 split only when the narrow cell contains a short status token and both cells remain readable after padding.

### 5-4. A4 — Terminal / Sequential Log

**Status:** pattern-derived composition using source-backed gradient and shadow primitives [EV-ARCA-ALLOW-001]. The complete log layout is not independently verified.

```html
<div style="max-width:900px;margin:0 auto;padding:20px 22px;background:linear-gradient(180deg,{TERMINAL_TOP},{TERMINAL_BOTTOM});border:1px solid {BORDER};box-shadow:inset 0 0 30px rgba(0,0,0,0.45);color:{TEXT};font-family:{FONT_MONO};">
  <div style="font-size:10px;letter-spacing:3px;color:{META};border-bottom:1px solid {BORDER};padding-bottom:10px;">SESSION · {SERIAL} · ACTIVE</div>
  <div style="font-size:13px;line-height:1.9;margin-top:14px;">
    <span style="color:{ACCENT};">00:01</span> First log entry<br>
    <span style="color:{ACCENT};">00:02</span> Second log entry<br>
    <span style="color:{META};">00:03</span> Muted system note
  </div>
</div>
```

Terminal identity may use green, amber, cyan, white, or another source-derived hue. Green is not mandatory.

### 5-5. A5 — Manuscript / Paper

**Status:** pattern-derived composition. Individual gradients and shadow layers may be source-backed [EV-ARCA-ALLOW-001], but the named paper material and complete anchor require exact-pattern evidence.

```html
<div style="max-width:820px;margin:0 auto;padding:34px 30px;background:linear-gradient(170deg,{PAPER_LIGHT},{PAPER_MID});border:1px solid {PAPER_BORDER};box-shadow:0 9px 18px rgba(50,32,10,0.28),inset 0 0 30px rgba(90,60,25,0.08);color:{INK};font-family:{FONT_SERIF};">
  <div style="text-align:center;border-top:1px solid {INK_MUTED};border-bottom:1px solid {INK_MUTED};padding:18px 12px;">
    <div style="font-size:11px;letter-spacing:4px;color:{INK_MUTED};">CHAPTER · RECORD · DATE</div>
    <div style="font-size:29px;font-weight:900;color:{INK};margin-top:8px;">Manuscript Title</div>
  </div>
  <div style="font-size:15px;line-height:2;color:{INK};padding:26px 4px 4px;">Manuscript body</div>
</div>
```

Use colored ink rather than neutral black for light surfaces. Verify the actual target theme after paste.

### 5-6. A6 — Split Ticket

**Status:** pattern-derived from allowed table-cell, dashed-border, and CTA vocabulary. The complete composition and fixed split are unverified.

```html
<div style="max-width:860px;margin:0 auto;background:linear-gradient(135deg,{SURFACE},{BG});border:1px solid {BORDER};color:{TEXT};font-family:{FONT_BODY};">
  <div style="display:table;width:100%;">
    <div style="display:table-cell;width:68%;vertical-align:middle;padding:24px 26px;">
      <div style="font-size:10px;letter-spacing:4px;color:{META};">ADMIT / DOWNLOAD / RELEASE</div>
      <div style="font-size:25px;font-weight:900;color:{HEADING};margin-top:8px;">Ticket Title</div>
      <div style="font-size:13px;line-height:1.75;color:{TEXT};margin-top:10px;">Short description</div>
    </div>
    <div style="display:table-cell;width:32%;vertical-align:middle;padding:22px;text-align:center;border-left:2px dashed {BORDER};">
      <div style="font-size:10px;color:{META};letter-spacing:2px;">SERIAL</div>
      <div style="font-size:16px;color:{HEADING};font-weight:800;margin:7px 0 12px;">{SERIAL}</div>
      <a href="{URL}" target="_blank" rel="noopener noreferrer" style="text-decoration:none;"><span style="display:inline-block;padding:10px 18px;background:linear-gradient(135deg,{ACCENT},{ACCENT_DARK});border-radius:6px;color:{CTA_TEXT};font-weight:800;">OPEN · 새 탭</span></a>
    </div>
  </div>
</div>
```

For long body copy on narrow screens, use a vertical ticket variant rather than preserving the split at all costs.

### 5-7. A7 — Broadcast / Status Board

**Status:** pattern-derived from allowed bands, tables, and callout vocabulary; the complete composition is unverified.

```html
<div style="max-width:900px;margin:0 auto;background:linear-gradient(160deg,{BG},{SURFACE});border:1px solid {BORDER};color:{TEXT};font-family:{FONT_BODY};">
  <div style="background:linear-gradient(90deg,{ACCENT_DARK},{ACCENT});padding:10px 18px;color:{CTA_TEXT};font-size:11px;font-weight:900;letter-spacing:3px;">CHANNEL · STATUS · LIVE</div>
  <div style="padding:26px 24px;border-bottom:1px solid {BORDER};">
    <div style="font-size:30px;line-height:1.25;font-weight:900;color:{HEADING};">Primary Headline</div>
    <div style="font-size:14px;line-height:1.8;color:{TEXT};margin-top:10px;">Essential summary</div>
  </div>
  <div style="display:table;width:100%;">
    <div style="display:table-cell;width:50%;padding:16px 20px;border-right:1px solid {BORDER};color:{TEXT};">STATUS A</div>
    <div style="display:table-cell;width:50%;padding:16px 20px;color:{TEXT};">STATUS B</div>
  </div>
</div>
```

Use saturated breaking-news bands sparingly. The broadcast family may also be calm, archival, or monochrome.

### 5-8. A8 — Technical Control Panel

**Status:** pattern-derived from allowed tables, borders, inline blocks, and shadows; the complete composition is unverified.

```html
<div style="max-width:900px;margin:0 auto;padding:22px;background:linear-gradient(160deg,{BG},{SURFACE});border:2px solid {BORDER};color:{TEXT};font-family:{FONT_MONO};">
  <div style="display:table;width:100%;border-bottom:1px solid {BORDER};padding-bottom:14px;">
    <div style="display:table-cell;vertical-align:middle;font-size:18px;font-weight:900;color:{HEADING};">SYSTEM PANEL</div>
    <div style="display:table-cell;vertical-align:middle;text-align:right;">
      <span style="display:inline-block;width:10px;height:10px;border-radius:50%;background:{STATUS_OK};box-shadow:0 0 8px {STATUS_OK};font-size:0;line-height:0;">&nbsp;</span>
      <span style="font-size:10px;color:{META};margin-left:8px;">ONLINE</span>
    </div>
  </div>
  <table style="width:100%;border-collapse:collapse;border:0;margin-top:16px;">
    <tbody>
      <tr>
        <th style="width:28%;text-align:left;padding:10px 12px;border:1px solid {BORDER};color:{META};font-size:11px;">INPUT</th>
        <td style="padding:10px 12px;border:1px solid {BORDER};color:{TEXT};font-size:13px;">Value or explanation</td>
      </tr>
      <tr>
        <th style="text-align:left;padding:10px 12px;border:1px solid {BORDER};color:{META};font-size:11px;">OUTPUT</th>
        <td style="padding:10px 12px;border:1px solid {BORDER};color:{TEXT};font-size:13px;">Result or status</td>
      </tr>
    </tbody>
  </table>
</div>
```

Status lamps require content such as `&nbsp;` so the editor does not remove the decorative element.

---

## 6. Shared Components

Use shared components as needed. Their presence is never a decoration quota.

### 6-1. Horizontal Split

```html
<div style="display:table;width:100%;">
  <div style="display:table-cell;width:50%;vertical-align:top;padding:16px;border-right:1px solid {BORDER};">Left</div>
  <div style="display:table-cell;width:50%;vertical-align:top;padding:16px;">Right</div>
</div>
```

Use a vertical structure when either side contains long text intended for mobile reading.

### 6-2. Collapsible Section

**Status:** source-backed primitive [EV-ARCA-ALLOW-001]. The complete styling remains pattern-derived.

```html
<details>
  <summary style="background:linear-gradient(90deg,{SURFACE},{BG});padding:14px 18px;font-size:13px;font-weight:700;color:{HEADING};border:1px solid {BORDER};">
    Section title <span style="float:right;color:{META};font-size:11px;font-weight:400;">상세 보기</span>
  </summary>
  <div style="padding:16px 18px;background:linear-gradient(180deg,{BG},{SURFACE});font-size:14px;color:{TEXT};line-height:1.8;border:1px solid {BORDER};border-top:0;">
    Hidden content
  </div>
</details>
```

This version keeps the browser's default disclosure indicator. If a verified target removes it, add a textual open/close cue rather than relying only on a decorative glyph.

### 6-3. Callout

```html
<div style="padding:17px 20px;background:linear-gradient(90deg,{CALLOUT_BG},{BG});border-left:3px solid {CALLOUT_COLOR};margin:16px 0;">
  <div style="font-size:11px;letter-spacing:3px;color:{CALLOUT_COLOR};font-weight:800;">{CALLOUT_LABEL}</div>
  <div style="font-size:14px;color:{TEXT};line-height:1.75;margin-top:7px;">Callout body</div>
</div>
```

Use text labels such as `안내`, `주의`, or `중요` so meaning is not conveyed by color alone.

### 6-4. Step Timeline

```html
<div style="border-left:2px solid {ACCENT};margin-left:8px;">
  <div style="padding:9px 0 9px 16px;border-bottom:1px solid {BORDER};">
    <span style="display:inline-block;width:22px;height:22px;line-height:22px;text-align:center;border-radius:50%;background:linear-gradient(135deg,{ACCENT},{ACCENT_DARK});color:{CTA_TEXT};font-size:11px;font-weight:800;margin-right:10px;">1</span>
    <span style="font-size:14px;color:{TEXT};">Step description</span>
  </div>
</div>
```

### 6-5. Screenshot Slot

**Status:** experimental until the exact empty-cell height pattern survives Arca save/reload.

An intentionally empty image cell is the exception to the decorative empty-element rule. Use `height`, not `min-height`, because CSS does not define a reliable `min-height` effect for table cells.

```html
<table style="width:100%;border-collapse:collapse;border:0;">
  <tbody>
    <tr>
      <td style="background:linear-gradient(90deg,{SURFACE},{BG});border:1px solid {BORDER};text-align:center;padding:8px 10px;font-size:12px;font-weight:700;color:{TEXT};">Screenshot caption</td>
    </tr>
    <tr>
      <td style="background:linear-gradient(180deg,{BG},{SURFACE});border:1px solid {BORDER};text-align:center;vertical-align:top;height:120px;"></td>
    </tr>
  </tbody>
</table>
```

Paste the image into the empty cell in the editor. Do not insert `&nbsp;` into this cell. Verify the empty height after save/reload before reusing this pattern; if it collapses, do not report the slot as verified.

### 6-6. Download CTA

```html
<div style="text-align:center;padding:20px 0;">
  <a href="{URL}" target="_blank" rel="noopener noreferrer" style="text-decoration:none;"><span style="display:inline-block;padding:13px 34px;background:linear-gradient(135deg,{ACCENT},{ACCENT_DARK});border:1px solid {ACCENT};border-radius:8px;color:{CTA_TEXT};font-size:14px;font-weight:800;">Download · 새 탭</span></a>
</div>
```

Put the button surface (background, padding, border, radius) on the child `inline-block` `span`, not on the `<a>`. Arca/Froala strips anchor inline style on save, so an anchor-styled button collapses to a plain underlined link after reload; the styled child survives. The child fills the clickable area, so the whole button remains clickable. Ensure `{CTA_TEXT}` contrasts with both gradient endpoints.

### 6-7. Code Block

```html
<div style="background:linear-gradient(180deg,{CODE_TOP},{CODE_BOTTOM});border:1px solid {BORDER};margin:16px 0;">
  <div style="display:table;width:100%;border-bottom:1px solid {BORDER};">
    <div style="display:table-cell;padding:8px 13px;color:{META};font-family:{FONT_MONO};font-size:11px;">filename.ext</div>
    <div style="display:table-cell;padding:8px 13px;text-align:right;color:{ACCENT};font-family:{FONT_MONO};font-size:10px;">LANG</div>
  </div>
  <pre style="margin:0;padding:16px;background:linear-gradient(180deg,{CODE_TOP},{CODE_BOTTOM});color:{CODE_TEXT};font-family:{FONT_MONO};font-size:12px;line-height:1.75;border:0;">code text</pre>
</div>
```

For editor-sensitive indentation, use `br` and `&nbsp;`. Do not place a `code` tag inside `pre`.

### 6-8. Meta Strip

```html
<table style="width:100%;border-collapse:collapse;border:0;">
  <tbody>
    <tr>
      <td style="width:33%;padding:10px;border:1px solid {BORDER};text-align:center;color:{TEXT};"><strong>VERSION</strong><br><span style="font-size:12px;color:{META};">1.0</span></td>
      <td style="width:34%;padding:10px;border:1px solid {BORDER};text-align:center;color:{TEXT};"><strong>TYPE</strong><br><span style="font-size:12px;color:{META};">MODULE</span></td>
      <td style="width:33%;padding:10px;border:1px solid {BORDER};text-align:center;color:{TEXT};"><strong>STATUS</strong><br><span style="font-size:12px;color:{META};">READY</span></td>
    </tr>
  </tbody>
</table>
```

Three short metadata cells are acceptable on mobile. Use two cells or stacked rows for longer labels and prose.

### 6-9. Gradient Frame

**Status:** verified primitive [EV-ARCA-ALLOW-001]. Gradient `border-image` and rounded corners do not combine reliably; use a square frame.

```html
<div style="border:3px solid {ACCENT};border-image:linear-gradient(45deg,{ACCENT},{ACCENT_DARK}) 1;padding:20px;color:{TEXT};">
  Framed content
</div>
```

### 6-10. Ruby / Furigana

```html
<ruby>東<rt>ひがし</rt></ruby><ruby>京<rt>きょう</rt></ruby>
```

---

## 6A. Impact Recipe Library

Impact recipes are compact visual moves, not required components or complete templates. Select them through the Scene Plan and Visual Ambition. All recipes below are **pattern-derived and production-eligible**; apply Section 1 and any local caution when implementing them.

Recipe composition guard:

- Use at most one primary recipe per scene.
- Do not combine two Hero recipes in the same first viewport.
- Add at most one supporting recipe, and only when it performs a different function.
- When two recipes compete for scale, framing, or focal priority, keep the stronger one.
- More recipes do not increase Visual Ambition. One memorable move is stronger than several weak effects.

| Function | Visual move | Best for | Compact recipe | Avoid |
|---|---|---|---|---|
| Hero | Giant Number Hero | Version introductions, chapter numbers, meaningful statistics | 30/70 or stacked composition; 56–88px number; quiet metadata; one strong divider | Inventing a meaningless number or crushing long text into a narrow split |
| Hero | Editorial Cover Stage | Major introductions, character releases, reports | Masthead or issue label; generous 40–64px breathing room; one large title; short deck; restrained rules | Repeating cover-scale treatment for every section |
| Hero | Poster Title Band | Announcements, launches, dramatic premises | Full-width ink or accent band; 30–44px title; short subtitle; one supporting motif | Long prose inside a saturated band or relying on glow alone |
| Transition | Quiet Chapter Break | Long guides, stories, multi-part documentation | 32–48px breathing zone; compact chapter label; symmetric or asymmetric rule; clear next-section title | Adding false chapters merely for decoration |
| Transition | Material Shift Bridge | Moving between premise, features, examples, and cautions | Keep the palette family; change one surface material; add a short transition label or divider | Switching to an unrelated design family or changing several materials at once |
| Emphasis | Pull Quote Stage | Core promise, testimonial, lore line, decisive warning | 22–30px quote; one edge rule or framed surface; compact attribution; strong whitespace | Enlarging ordinary body copy or using multiple competing quotes together |
| Emphasis | Dossier Stamp Alert | Compatibility notes, classifications, verification status | Inline-block serial or stamp; 2px border; spaced label; paired explanatory text | Defaulting every archive to red `CLASSIFIED` or conveying meaning by color alone |
| Navigation | Folder Tree Route | Module maps, file bundles, nested topics | Monospace labels; shallow indentation; `br` and `&nbsp;` only where preservation matters; one active route marker | Fake interactive navigation, deep nesting, or invented filenames |
| Metadata | Status Rail / Lower Third | Releases, broadcasts, system summaries | Two or three short cells; textual state labels; one narrow signal band; explicit borders | Four dense prose columns, color-only states, or permanent breaking-news intensity |
| Finale | Ticket Cutline CTA | Downloads, event links, release handoffs | Compact serial; dashed separator; one anchor-wrapped padded child; use a vertical variant for long mobile content | Fixed 68/32 split with long labels or multiple equal-priority CTAs |
| Finale | Download Finale | Asset packs, tools, updates | Generous closing space; filename and version; one primary CTA; short compatibility note | Repeating the hero, hiding the filename, or surrounding the CTA with unrelated badges |
| Finale | Credits / Release Footer | Attribution, closing notes, version history | Centered or editorial sequence; muted but readable metadata; one closing rule or motif | Tiny low-contrast credits or an ornate footer larger than the main content |

Use a recipe because it performs a visual function in the scene. Do not add one merely to increase the count. In Showcase mode, prefer one or two strong recipes over many weak effects.

---

## 6B. Concrete Aesthetic Baselines

These mini compositions calibrate color-role separation, Korean typography, and first-viewport hierarchy. They are **pattern-derived and production-eligible**, not default templates or proof of aesthetic superiority. Do not copy a complete palette merely because it is concrete; adapt its role logic to the content and reject it when the source points elsewhere.

### B1 — Contemporary Neutral

Palette: `BG #eef3f8 / SURFACE #ffffff / TEXT #253247 / BORDER #b8c6d6 / ACCENT #245fcc`  
Primary move: Editorial Cover Stage. Why it works: 차가운 중성 면을 명확히 분리하고 선명한 파랑 하나로 위계를 만듭니다. Failure signs: 콘텐츠 모티프 없는 범용 소프트웨어 파랑, 테두리 카드의 과도한 반복.

```html
<div style="max-width:900px;margin:0 auto;background:linear-gradient(160deg,#eef3f8,#f8fafc);border:1px solid #b8c6d6;color:#253247;font-family:'Pretendard','Noto Sans KR','Apple SD Gothic Neo','Malgun Gothic',sans-serif;">
  <div style="padding:42px 30px 34px;border-bottom:1px solid #b8c6d6;">
    <div style="font-size:11px;letter-spacing:3px;color:#245fcc;font-weight:800;">ACARCA GUIDE · 6.5</div>
    <div style="font-size:36px;line-height:1.25;color:#142033;font-weight:900;margin-top:10px;">안전 규격을 넘어<br>보기 좋은 결과까지</div>
    <div style="max-width:620px;font-size:14px;line-height:1.85;color:#526176;margin-top:15px;">명확한 정보 위계와 한 번의 강한 초점으로 구성한 현대적 기준안입니다.</div>
  </div>
  <div style="padding:24px 30px;background:#ffffff;border-top:4px solid #245fcc;">
    <div style="font-size:17px;font-weight:800;color:#142033;">핵심 원칙</div>
    <div style="font-size:14px;line-height:1.8;color:#253247;margin-top:7px;">밝은 배경, 흰 표면, 진한 본문, 선명한 초점색의 역할을 섞지 않습니다.</div>
  </div>
</div>
```

### B2 — Editorial Monochrome

Palette: `BG #f2f1ed / SURFACE #fffefa / TEXT #282b30 / BORDER #b8b6af / ACCENT #111827`  
Primary move: Giant Number Hero. Why it works: 힘을 색 대신 숫자 크기, 검정 잉크, 비대칭 여백에서 얻습니다. Failure signs: 의미 없는 거대 숫자, 한글과 영문 세리프의 불일치, 누런 중간색 남용.

```html
<div style="max-width:860px;margin:0 auto;background:linear-gradient(165deg,#f2f1ed,#fffefa);border-top:5px solid #111827;border-bottom:1px solid #b8b6af;color:#282b30;font-family:'Noto Serif KR','Nanum Myeongjo','AppleMyungjo','Batang',serif;">
  <div style="padding:30px 28px 22px;border-bottom:1px solid #b8b6af;">
    <span style="font-size:68px;line-height:1;color:#111827;font-weight:900;">6.5</span>
    <span style="display:inline-block;font-size:10px;letter-spacing:3px;color:#666a70;margin-left:12px;">EDITION</span>
  </div>
  <div style="padding:30px 28px 34px;">
    <div style="font-size:29px;line-height:1.35;color:#202329;font-weight:800;">아카라이브 HTML 제작 기준</div>
    <div style="font-size:14px;line-height:1.9;color:#4d5158;margin-top:12px;">장식보다 편집 리듬과 정보의 우선순위가 먼저 보이도록 구성합니다.</div>
  </div>
</div>
```

### B3 — Warm Material with Crisp Focus

Palette: `BG #d9bd91 / SURFACE #fffaf0 / TEXT #3d332a / BORDER #b58d5b / ACCENT #b64032`  
Primary move: Material Shift Bridge. Why it works: 보드와 종이의 명도를 분리하고 붉은 잉크 하나로 초점을 만듭니다. Failure signs: 배경·카드·테두리가 모두 비슷한 베이지, 갈색 글자 다섯 종류, 금색을 고급스러움의 증거로 사용.

```html
<div style="max-width:880px;margin:0 auto;padding:28px;background:linear-gradient(145deg,#d9bd91,#d1ad78);border:1px solid #b58d5b;box-shadow:inset 0 0 46px rgba(91,58,25,0.18);color:#3d332a;font-family:'Pretendard','Noto Sans KR','Apple SD Gothic Neo','Malgun Gothic',sans-serif;">
  <div style="background:linear-gradient(170deg,#fffaf0,#fff6e7);border:1px solid #b58d5b;padding:30px 28px;box-shadow:0 8px 18px rgba(73,48,24,0.20);">
    <div style="display:inline-block;padding:5px 9px;border:2px solid #b64032;color:#b64032;font-size:10px;font-weight:900;letter-spacing:2px;">RELEASE NOTE</div>
    <div style="font-size:30px;line-height:1.3;color:#2f2923;font-weight:900;margin-top:14px;">따뜻하지만 흐리지 않게</div>
    <div style="font-size:14px;line-height:1.9;color:#4b4036;margin-top:11px;">크림색 표면과 짙은 잉크 사이에 충분한 차이를 두고, 강조색은 한곳에 집중합니다.</div>
  </div>
</div>
```

### B4 — Atmospheric Dark

Palette: `BG #07111f / SURFACE #10233a / TEXT #d9e8f4 / BORDER #31516d / ACCENT #55c7d9`  
Primary move: Pull Quote Stage. Why it works: 검푸른 세 단계와 밝은 청록 한 점으로 깊이를 만듭니다. Failure signs: 모든 글자가 청록색, 과도한 glow, 터미널 문법과 심해 문법의 무의미한 혼합.

```html
<div style="max-width:900px;margin:0 auto;background:linear-gradient(160deg,#07111f,#10233a,#081522);border:1px solid #31516d;color:#d9e8f4;font-family:'Pretendard','Noto Sans KR','Apple SD Gothic Neo','Malgun Gothic',sans-serif;box-shadow:inset 0 0 42px rgba(2,8,16,0.48);">
  <div style="padding:38px 30px 28px;border-bottom:1px solid #31516d;">
    <div style="font-size:10px;letter-spacing:4px;color:#55c7d9;font-weight:800;">DEPTH · SIGNAL · RECORD</div>
    <div style="font-size:34px;line-height:1.25;color:#f1f8fc;font-weight:900;margin-top:12px;">빛은 한곳에만 남긴다</div>
  </div>
  <div style="margin:24px 30px 30px;padding:20px 22px;background:linear-gradient(90deg,#10233a,#0b1b2d);border-left:4px solid #55c7d9;">
    <div style="font-size:21px;line-height:1.55;color:#eef9fc;font-weight:700;">어두운 디자인의 힘은 네온 개수가 아니라 명암의 깊이에서 나온다.</div>
    <div style="font-size:11px;color:#8da9bd;margin-top:10px;">VISUAL CALIBRATION NOTE</div>
  </div>
</div>
```

### B5 — Vivid System

Palette: `BG #10131c / SURFACE #1c2331 / TEXT #edf2f8 / BORDER #43506a / ACCENT #f3a712`  
Primary move: Status Rail / Lower Third. Why it works: 거의 무채색인 시스템 면 위에 호박색 신호만 제한적으로 사용합니다. Failure signs: 모든 셀을 강조색으로 채우기, 상태를 색으로만 구분하기, 작은 메타데이터를 지나치게 촘촘하게 배치하기.

```html
<div style="max-width:900px;margin:0 auto;background:linear-gradient(160deg,#10131c,#1c2331);border:2px solid #43506a;color:#edf2f8;font-family:'Pretendard','Noto Sans KR','Apple SD Gothic Neo','Malgun Gothic',sans-serif;">
  <div style="background:linear-gradient(90deg,#d88d00,#f3a712);padding:9px 18px;color:#17130a;font-size:11px;font-weight:900;letter-spacing:3px;">SYSTEM · READY · 6.5</div>
  <div style="padding:28px 26px 22px;">
    <div style="font-size:31px;line-height:1.25;color:#ffffff;font-weight:900;">제작 흐름을 한눈에</div>
    <div style="font-size:14px;line-height:1.8;color:#c7d0dc;margin-top:10px;">기능 설명은 차분하게 유지하고 실제 상태와 행동만 선명하게 표시합니다.</div>
  </div>
  <table style="width:100%;border-collapse:collapse;border:0;">
    <tbody><tr>
      <td style="width:50%;padding:13px 16px;border:1px solid #43506a;color:#edf2f8;"><strong>상태</strong><br><span style="font-size:11px;color:#aeb9c8;">사용 가능</span></td>
      <td style="width:50%;padding:13px 16px;border:1px solid #43506a;color:#edf2f8;"><strong>출력</strong><br><span style="font-size:11px;color:#aeb9c8;">HTML 단일 파일</span></td>
    </tr></tbody>
  </table>
</div>
```

---

## 7. Palette, Type, and Material Tokens

### 7-1. Palette Derivation

Derive each post palette from supplied material instead of copying a complete example palette.

Source priority:

1. representative character art, screenshots, or background art;
2. logo and established brand colors;
3. setting objects and material metaphors;
4. genre convention when no stronger source exists.

Record the source in the cumulative brief. In Delegated Design mode, the model may select the most representative source and proceed.

Genre labels are weak evidence, not palette sources. `Editorial`, `fantasy`, `premium`, or `technical` does not by itself justify beige, gold, neon, cyan, or any other habitual palette.

### 7-2. Source-Less Palette Protocol

When the content has no natural color source, do not commit to the first genre cliché. Create three concrete candidates with literal colors for `BG / SURFACE / TEXT / BORDER / ACCENT`.

- vary temperature, lightness structure, and focal strategy—not hue alone;
- include one contemporary neutral strategy, one restrained warm or cool strategy, and one higher-contrast alternative when appropriate;
- Guided Choice exposes compact swatches or visual samples;
- Delegated Design compares them internally and selects through the Palette Quality Gate;
- no candidate becomes a permanent default for future unrelated work.

### 7-3. Palette Quality Gate

Before selecting or praising a palette, check the rendered result when rendering is available and otherwise inspect the concrete role values:

- Is the palette derived from a real source rather than a genre cliché?
- Do background, surface, text, border, and accent have visibly separated roles?
- Are several warm neutrals collapsing into one muddy beige or brown field?
- Is the entire design trapped in middle lightness without a clear dark, light, and focal relationship?
- Does the accent create a real focal contrast instead of behaving like another muted surface color?
- Do heading, body, border, and CTA colors follow one coherent temperature and hue logic?
- If the palette is restrained, do scale, spacing, typography, material contrast, or composition provide sufficient force?

Do not call a palette refined merely because every color is muted. Low saturation is a valid choice, not proof of quality.

**Luxury trope guard:** ivory, serif type, wide letter-spacing, gold, burgundy, and rust do not make a design premium by themselves. Do not label a direction `luxury`, `high-end`, `premium`, or `most polished` until its rendered composition, Korean typography fallback, palette balance, and first viewport have been visually inspected. If rendering is unavailable, describe the intended direction without certifying aesthetic superiority.

### 7-4. Core Token Roles

| Token | Role |
|---|---|
| `BG` | Page or anchor base |
| `SURFACE` | Panels and secondary planes |
| `CARD_A`, `CARD_B` | Optional card surfaces |
| `BORDER`, `BORDER_A`, `BORDER_B` | Structural separation |
| `HEADING` | Primary titles |
| `TEXT` | Body copy |
| `META` | Timestamps, labels, secondary information |
| `ACCENT` | Main signal color |
| `ACCENT_DARK` | Gradient partner or deeper accent |
| `CTA_TEXT` | Text on accent-filled controls |
| `STATUS_OK` | Explicit positive state only |

For light paper designs also define `PAPER_LIGHT`, `PAPER_MID`, `PAPER_BORDER`, `INK`, and `INK_MUTED`.

For code or terminal surfaces define `TERMINAL_TOP`, `TERMINAL_BOTTOM`, `CODE_TOP`, `CODE_BOTTOM`, and `CODE_TEXT` as needed.

### 7-5. Contrast and Hierarchy

- Body text SHOULD reach at least 4.5:1 contrast against its immediate surface when a contrast tool is available.
- Large display text SHOULD reach at least 3:1.
- Do not use `ACCENT` for multi-sentence body prose merely to make the page colorful.
- `META` must remain readable; muted does not mean nearly invisible.
- `CTA_TEXT` must work against both ends of its gradient.
- A restrained or desaturated accent is valid when it suits the source. Do not force a saturation threshold that contradicts the intended mood.
- `ACCENT_DARK` may share the main hue for a smooth gradient, but an intentional multi-hue gradient is also valid. Judge contrast and purpose rather than enforcing an arbitrary hue-distance gate.

The familiar 60/30/10 ratio is a composition heuristic, not a measurable build blocker:

- roughly 60% background and surfaces;
- roughly 30% text and neutral information;
- roughly 10% accents, CTAs, and signals.

### 7-6. Palette Multiplicity

| Mode | Definition | Typical use |
|---|---|---|
| Monochrome | One accent family plus neutrals | Minimal, archival, terminal |
| Dual-tone | Main accent plus one controlled secondary hue | General-purpose hierarchy |
| Triadic | Three meaning-assigned hues | Rosters, game UI, multi-category catalogs |

For multi-hue designs, assign a semantic role to each hue. Do not scatter secondary colors randomly across unrelated components.

### 7-7. Dark and Light Bases

**Dark base guidance:**

- use a near-black colored background rather than pure black when the source permits;
- keep body text comfortably bright without making every label white;
- reserve the brightest color for headings, selected states, and CTAs;
- test glow against actual body copy density.

**Light base guidance:**

- use warm or colored ink rather than neutral `#000`–`#555` when prior Arca dark-theme overrides would damage the design;
- prefer a subtle gradient paper surface when flat backgrounds are theme-sensitive;
- include a light-theme optimization notice only when the final result genuinely depends on light mode;
- verify both themes after paste instead of assuming an HTML entity or inline color defeats every client override.

Example notice:

```html
<div style="font-size:12px;color:#8f5264;text-align:center;padding:6px 12px;background:linear-gradient(90deg,rgba(200,80,130,0.08),rgba(200,80,130,0.15),rgba(200,80,130,0.08));font-weight:700;border:1px solid rgba(200,80,130,0.18);">&#8251; 본 소개글은 라이트 모드에 최적화되어 있습니다.</div>
```

### 7-8. Material Recipes

| Material | Status | Compact recipe |
|---|---|---|
| Parchment | pattern-derived | warm dark gradient, 1px brown border, restrained inset shadow |
| Terminal glass | pattern-derived | near-black gradient, inset depth, mono type, controlled text-shadow |
| Velvet | pattern-derived | deep colored gradient with a soft colored inset shadow |
| Cork board | pattern-derived | warm tan gradient with vignette and inset frame shadows |
| Cream paper | pattern-derived | cream gradient, explicit paper border, warm drop shadow |
| Pushpin | pattern-derived | non-empty circular `div`, explicit dimensions, multi-inset shadow |
| Glass-like panel | pattern-derived | pale gradient, thin border, soft drop shadow; no backdrop filter |
| Metal panel | pattern-derived | dark vertical gradient, sharp border, engraved text-shadow |
| Masking tape | pattern-derived | translucent warm strip with a simple border; no rotation |
| Blueprint panel | pattern-derived | navy surface, cyan rules, coordinate labels; no generated grid dependency |

The supplied reference supports exact gradient, shadow, border, and non-empty decorative-element primitives [EV-ARCA-ALLOW-001], but it does not by itself verify every named material recipe above. Do not call a complete material or composition verified merely because its individual primitives are verified.

### 7-9. Korean Typography and Font Stacks

Use fallback chains and ensure the layout survives when the first font is unavailable.

```text
Korean sans: 'Pretendard','Noto Sans KR','Apple SD Gothic Neo','Malgun Gothic',sans-serif
Korean serif: 'Noto Serif KR','Nanum Myeongjo','AppleMyungjo','Batang',serif
Code / system: 'Consolas','Monaco','Courier New',monospace
```

Do not rely on Latin serif fonts such as Cambria, Georgia, or Times New Roman to define Korean title identity; they may style Latin characters while Hangul falls back to a visually unrelated system font. Verify the actual Korean fallback appearance when rendering is available.

Custom web fonts and the named Korean fonts cannot be assumed because external loading is blocked and device installations differ. Typography identity must survive through size, weight, line-height, spacing, rules, and composition rather than one preferred font name.

### 7-10. Symbols and Entities

HTML numeric entities can reduce source-copy corruption for many symbols:

```text
&#9888; warning     &#10003; check     &#9432; info
&#10022; star       &#9472; line       &#9656; triangle
```

Entities do not guarantee that every client has a matching glyph or emoji font. Prefer ASCII or common BMP symbols for functional labels, and always include a text label when the symbol carries meaning.

---

## 8. Mobile and Accessibility

Arca posts must remain usable around a 360px content width without relying on `@media` or JavaScript.

### 8-1. Column Budget

| Content type | Mobile-safe default |
|---|---:|
| Text cards or profiles | 1–2 columns |
| Short metadata | Up to 3 columns |
| Tiny numeric/icon stats | Up to 4 columns with minimal padding |
| Long labels, code, dialogue | 1 column |

Four 90px cells do not provide 80px of content space after normal card padding. Do not use the raw `360 ÷ column count` result without subtracting borders and horizontal padding.

Table cells do not automatically stack. If stacking is essential, author a vertical structure from the start or provide a separate mobile-oriented variant.

Fixed asymmetric splits such as 68/32 are not mobile-safe by default. Use them only for short labels and compact metadata; choose a vertical variant when either side contains long Korean text, a wide CTA, or an essential explanation.

Layout tables may be announced as data tables by assistive technology. Use real `th`/`td` relationships only for tabular data. For visual card rows, keep DOM reading order logical, place headings and explanations in the cells themselves, and do not depend on visual column position to communicate meaning. Do not add `role="presentation"` or other ARIA solely by assumption; first verify that the target preserves it.

### 8-2. Type and Spacing

- Body copy SHOULD be at least 14px.
- Metadata SHOULD normally be at least 11–12px.
- Body line-height SHOULD generally remain between 1.65 and 2.0.
- Avoid large letter-spacing on Korean body text.
- Do not combine tiny type, low contrast, and dense padding in the same component.
- Long Korean labels SHOULD use natural line breaks or a wider row.
- Normal text SHOULD reach at least 4.5:1 contrast. Text at least 24px regular or about 18.7px bold MAY use the 3:1 large-text threshold.
- If no contrast tool or measured source is available, use a conservative high-contrast pair and do not claim measured WCAG compliance.

### 8-3. Links and Actions

- A link SHOULD be distinguishable by more than color alone: underline, border, button surface, or clear link context.
- A primary CTA SHOULD provide at least a 24×24 CSS px clickable target or equivalent spacing from adjacent targets. In Arca/Froala, put the padding and button surface on an `inline-block` child inside the anchor.
- The styled child MUST remain inside the anchor so the entire visible button is clickable; do not use an unlinked decorative sibling as the button surface.
- Do not remove the browser's keyboard focus indicator. Because `outline` is blocked in this target, preserve the native focus treatment instead of attempting `outline:none` or an unverified replacement.
- When `target="_blank"` materially changes the interaction, include visible wording such as `새 탭` or state it in the surrounding link context.
- External URLs MUST use `https:` unless a supplied target requires another safe scheme.
- Reject or neutralize `javascript:` and unknown executable schemes.

### 8-4. Images and Media

- Provide meaningful alternative text when the editor workflow supports it.
- Keep essential instructions outside images.
- Do not place a wide image beside long text in a fixed split on mobile.
- Screenshot captions use readable text colors, not accent-only prose.
- Image insertion slots remain intentionally empty until the user pastes media in the editor.
- Use `video` or `iframe` only for a user-supplied or explicitly approved trusted HTTPS source.
- Do not invent embed URLs or assume that a chat preview proves the embed survives Arca save/reload.

### 8-5. Meaning Beyond Color

Warnings, success states, selected states, and categories SHOULD include a text label, number, or shape difference. A red/green distinction alone is insufficient.

---

## 9. Content Recipes

Recipes specify necessary information, not visual layouts. Choose the design independently through Sections 3–5.

### 9-1. Character Bot

- title, premise, and character identity;
- representative image slots;
- role, personality, setting, and interaction hook;
- first-message or scenario samples;
- usage notes and content notices where relevant;
- download/share links and credits.

Common directions: A1 Editorial, A2 Catalog, A3 Archive, A5 Manuscript, GAM-02 Character Select, or a content-derived custom design.

### 9-2. Utility Module or Extension

- one-line purpose and supported environment;
- key features;
- installation steps;
- configuration or toggle matrix;
- before/after screenshots;
- usage examples;
- download, version, and changelog.

Common directions: A1 Editorial, A6 Ticket, A8 Control Panel, TEC-02 Blueprint, or COM-01 Catalog.

### 9-3. Worldbuilding or NSFW Module

- concept and scope;
- setting rules;
- major character or faction sheets;
- content notices and boundaries;
- sample responses;
- installation and required lorebook/regex assets.

Choose a design that matches the world rather than automatically using Dossier.

### 9-4. Fork or Modified Edition

- original source and author credit;
- compatibility and prerequisites;
- exact changes;
- migration or replacement warnings;
- versioned download;
- changelog.

### 9-5. Guide or Tutorial

- goal and audience;
- prerequisites;
- numbered procedure;
- code or configuration blocks;
- screenshots or before/after examples;
- troubleshooting and rollback notes;
- result verification.

### 9-6. Asset Pack or Multi-Item Release

- category and item count;
- preview slots;
- naming and format conventions;
- usage/licensing notes;
- download bundle and version;
- individual item notes when needed.

Catalog, Museum Archive, Pinboard, Character Select, and custom collection metaphors are all valid.

### 9-7. Patch Notes or Announcement

- version/date;
- short summary;
- added, changed, fixed, and known issues as applicable;
- migration or breaking-change warning;
- download/update CTA.

Minimal patch notes do not need decorative callouts or multiple header archetypes unless the content benefits.

---

## 10. Build Procedure

### 10-1. Before HTML

1. Route the task using Section 0-2.
2. Assemble the cumulative brief.
3. Resolve design authority and Visual Ambition independently.
4. Inventory supplied content and preserve exact filenames, URLs, version numbers, and user wording where required.
5. Derive the content essence, metaphor, information flow, and Design DNA.
6. Compare candidates when Guided or Delegated rules require it.
7. Create the Scene Plan for Standard or Full work.
8. Select an anchor or safe custom structure.
9. Define all palette and type tokens; run the Source-Less Palette Protocol when no real source exists.
10. Apply the Palette Quality Gate and Recipe Composition Guard before committing to the candidate.

Do not emit this internal procedure in Final paste-ready mode.

### 10-2. During HTML

- Build the information hierarchy before decoration.
- Use only live-HTML vocabulary from Section 1.
- Replace every token with a literal safe value.
- Keep content-required repetition consistent.
- Follow the Scene Plan; create purposeful contrast between different information roles and preserve repeated forms for repeated content.
- Use Impact Recipes only where they perform a clear visual function.
- Prefer one coherent design family over a collage of unrelated motifs.
- Preserve user-provided links, filenames, labels, and text unless editing them was requested.
- Mark missing images and URLs as placeholders in Draft mode; do not invent real resources.

### 10-3. Paste-Ready Cleanup

- remove all HTML comments;
- remove Markdown fences and prose outside the artifact;
- replace all `{TOKENS}` and placeholder URLs;
- remove unused empty decorative elements or fill them with `&nbsp;`;
- preserve intentionally empty screenshot cells;
- verify balanced tags and table structure;
- ensure the first and last bytes belong to the requested HTML payload.

---

## 11. Validation

### 11-1. Mechanical Checks

Inspect actual tags and live style attributes for:

- blocked tags from Section 1-1;
- `display:flex`, `display:grid`, `display:inline-grid`, `grid-template-*`, position, z-index, overflow, gap, opacity, filter, transform, animation, transition, outline, and outline-offset;
- `url()` in any live CSS value;
- selectors, at-rules, and HTML comments;
- event handlers and unsafe URL schemes;
- unreplaced `{TOKENS}`, fake URLs, filenames, or placeholder labels;
- empty decorative `span`/`div` elements;
- malformed nesting or unbalanced tags;
- missing explicit table/cell borders where editor defaults would leak through;
- card grids lacking `border-collapse:separate`, `border-spacing`, or table `border:0`;
- accidental content in screenshot insertion cells;
- screenshot cells that rely on `min-height` or are reported as verified without save/reload evidence;
- text-card rows that exceed the mobile column budget;
- tiny, low-contrast, or accent-colored body prose;
- color-only warnings and controls.

Do not scan displayed code text as if it were a live style attribute.

### 11-2. Intent and Design Checks

- Does the result contain every requested content item?
- Does the chosen direction match the confirmed or delegated brief?
- Were design authority and Visual Ambition inferred separately?
- If `더 예쁘게` rejected an existing aesthetic, was the direction genuinely reconsidered rather than recolored?
- Is a safe baseline being rejected merely for being familiar? If so, restore it when it is the better fit.
- If originality was requested, is there at least one meaningful content-specific decision?
- If Showcase was requested, is there a memorable focal scene without assuming that Showcase requires neon, vivid color, or dense decoration?
- Does one dominant family remain visible throughout the post?
- Are repeated elements consistent where consistency communicates meaning?
- Do variation and decoration improve scanning instead of satisfying a quota?
- Is the most important information obvious in the first screenful?
- Do section differences come from information roles rather than arbitrary styling changes?
- Does each material or motif connect to the content metaphor?
- Is `gradient + bordered card` being repeated from habit when another information structure would fit better?
- Did internal or visible candidates differ structurally, rather than only by palette?
- Do header roles reflect information hierarchy rather than cosmetic color swaps?
- Does the palette pass Section 7-3 without muddy role collapse or unsupported luxury tropes?
- Does each scene have no more than one primary recipe and one non-competing support recipe?
- Does Korean typography remain coherent when preferred Latin or Korean fonts are unavailable?
- If decoration is mentally removed, does the information hierarchy still hold?

### 11-3. Aesthetic Preview Gate

Do not rank a candidate as `best`, `premium`, `luxury`, `high-end`, or `most polished` from source code alone.

When rendering is available:

1. inspect the first viewport near desktop width;
2. inspect a version near 360px content width;
3. check palette mass, surface separation, Korean fallback, focal hierarchy, recipe conflicts, wrapping, and overflow;
4. revise or reject the candidate before making an aesthetic ranking.

When rendering is unavailable:

- do not certify aesthetic superiority;
- compare literal palette roles, first-viewport structure, Korean fallback, and recipe conflicts against Sections 6A, 6B, and 7;
- Guided Choice exposes compact visual samples to the user;
- Delegated Design may proceed without another question, but selects provisionally from the concrete quality criteria rather than claiming that it has visually inspected the result.

An aesthetic preview is not Arca sanitizer verification. A chat or local browser render may judge composition, while only the target environment can prove save/reload survival.

### 11-4. Environment Checks

When practical, inspect:

1. editor state after paste;
2. saved post after reload;
3. PC light theme;
4. PC dark theme;
5. mobile width;
6. expanded and collapsed `details` states;
7. links, image slots, tables, and code blocks.

If only a chat preview was observed, label conclusions as preview-specific. Do not report Arca verification without an Arca observation.

### 11-5. Review Layers

Do not collapse these three activities into one claim:

- **Self-check** — the builder runs Sections 11-1 and 11-2 to reduce omissions. This is useful but is not independent proof.
- **Environment verification** — the result is observed after actual Arca paste, save, and reload in the stated theme/device.
- **External audit** — a separate human, model instance, or tool reviews the result without inheriting the builder's assumptions.

The same model MUST NOT treat its own final review as proof of correctness. External audit can find design or reasoning defects, but only target-environment observation can prove sanitizer survival.

### 11-6. Behavior Test Prompts

Use these scenarios when auditing a future guide revision:

| Scenario | Expected route |
|---|---|
| `이 자료로 소개글 만들어줘` plus materials | Use materials; Targeted or Guided only if a blocking choice remains |
| `디자인 후보부터 보여줘` | Guided Choice; text concepts first |
| `알아서 어울리게 만들어줘` | Delegated Design; proceed without preference interview |
| `디자인을 좀 더 예쁘게 가능해?` after an unsatisfying result | Aesthetic revision; Guided visual samples unless authority was delegated |
| `알아서 예쁘게 만들어줘` | Delegated + Showcase; compare three internal candidates, then build |
| `시안부터 예쁘게 만들어줘` | Guided + Showcase; show three compact visual samples and wait |
| `화려하지 않게 인상적으로` | Showcase through scale, spacing, type, material, or composition rather than vivid color |
| `색상 자료는 없으니 알아서 예쁘게` | Delegated + Showcase; compare three literal role palettes and reject genre clichés |
| `고급스러운 안이 뭐야?` without rendering capability | Describe provisional intent; do not certify `premium` or rank by source code alone |
| Direction, materials, and final mode supplied | Direct Build |
| Partial explicit direction | Targeted Interview for only blocking fields |
| Existing HTML bug report | Debug route; no design interview |
| Review request | Review route; no unauthorized rewrite |
| Final paste-ready request | HTML-only artifact after validation |

---

## 12. Verification and Patch Protocol

This section governs changes to this guide, not normal post authoring.

### 12-1. Ground Truth

A verified claim traces to a specific source:

- an Arca post confirmed after save/reload;
- a supplied HTML file explicitly identified as passing the target environment;

Record the source path or URL, observation environment, theme/device when relevant, and date. If no source exists, use `pattern-derived` or `experimental` rather than inventing verification.

Every specific `Status: verified` label MUST include an evidence ID such as `[EV-ARCA-ALLOW-001]`. A version number, inherited wording, or a successful chat preview is not an evidence ID.

### 12-2. Source Fidelity

When extracting a verified pattern:

- preserve tag identity unless a change is explained;
- preserve sanitizer-relevant attributes on the element where they were verified;
- preserve `&nbsp;`, explicit zero values, and empty-slot exceptions;
- genericize content values only when the change is documented;
- do not move a verified cell border to an inner wrapper without re-verification.

Formatting whitespace may change for readability.

### 12-3. No Inference Promotion

Verification of one feature does not verify a family of similar features. Keep claims as narrow as the evidence.

Examples:

- two inset shadows working verifies that exact multi-inset use, not arbitrary layer counts;
- a linear gradient working does not verify conic gradients;
- a pushpin `div` surviving does not verify an empty `span` replacement;
- desktop survival does not prove mobile readability.

### 12-4. Patch Workflow

1. cite the verified source or assign a non-verified status;
2. extract the smallest reusable pattern;
3. compare tags, attributes, and placeholder content with the source;
4. run Section 11 mechanical checks on new live examples;
5. verify cross-references and code-fence balance;
6. update the changelog without broadening claims;
7. preserve previous version files.

### 12-5. Evidence Record

Use this compact format for future verified additions:

```text
Pattern:
Status:
Source:
Observed environment:
Theme/device:
Date:
Exact feature proven:
Known limits:
```

Current evidence index:

```text
ID: EV-ARCA-ALLOW-001
Pattern: exact live sanitizer primitives preserved in the supplied allowed-style reference
Status: verified only for the exact live declarations present in that file
Source: 아카라이브_허용_HTML_스타일.html (user-supplied reference; not bundled with this guide)
Observed environment: identified by the source as Arca/Froala save-reload testing
Theme/device: not recorded in the available metadata
Date: 2026-03 (day not recorded)
Exact feature proven: display:inline-block; display:inline-flex; linear-gradient(); radial-gradient(); rgba(); multi text-shadow; multi/inset/ring box-shadow; border-image; border-radius; details/summary without list-style:none; ruby/rt; styled blockquote inner wrapper; non-empty &nbsp; decoration; table tags with inline styles
Known limits: header says 3 verification passes while footer says 2; calc(), clamp(), and CSS var() appear only in explanatory text, not saved live style declarations; no exact proof for empty screenshot-cell height, display:table/table-cell, float:right, border-spacing, or a complete anchor/material composition
```

Until the source metadata conflict is corrected, preserve the exact-feature evidence above but do not claim a settled verification-pass count.

---

## 13. Final Routing Index

This is a navigation index, not a second set of rules.

1. Route the task: Section 0-2.
2. Resolve design authority, Originality, and Visual Ambition: Sections 0-4 through 0-9.
3. Apply sanitizer constraints: Section 1.
4. Size the build without decoration quotas: Section 2.
5. Derive Design DNA and a Scene Plan before consulting the registry: Section 3.
6. Route registry directions to anchors: Sections 4–5.
7. Add only necessary components and purposeful Impact Recipes; use concrete baselines for calibration, not copying: Sections 6, 6A, and 6B.
8. Resolve palette, Korean type, and material tokens through the quality gates: Section 7.
9. Respect mobile and accessibility limits: Section 8.
10. Validate content, aesthetic preview claims, live HTML, and environment: Section 11.
11. In Final paste-ready mode, return HTML only.

---

> Special Thanks: https://arca.live/b/characterai/167022655
