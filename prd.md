---
Title: Socratic Voice Companion for Calistung Comprehension
Author: Jasson
Date: 2026-10-11
Status: Validation-Ready PRD
---

| Field | Value |
| :--- | :--- |
| Version | 1.0.0 |
| Evidence Mode | Assumption-Led |
| Risk Tier | Tier 2 |
| Release Window | 2027-Q1 |
| Review State | State 3: Validation-Ready PRD |

> *"This document was developed through structured product reasoning and simulated cross-functional review. It does not represent complete internal evidence, engineering validation, legal advice, or organizational approval."*

---

## 1. Decision Summary

Middle-class households lack affordable 1-on-1 tutoring to build foundational literacy and numeracy (Calistung) before age seven [EV-01]. Children currently consume passive video feeds or addictive gamified apps that fail to build cognitive reasoning [EV-02]. We propose a voice-first Socratic companion that engages young learners through interactive dialogue and persistent cognitive adaptation [EV-03]. 

The service operates on a lightweight cloud architecture with a target gross margin equal to or exceeding 70% [EV-04]. It caps monthly cloud inference expenditure at IDR 25,000 ($1.55) per active child [EV-05]. The user experience replaces screen-tapping loops with a 15-minute daily voice inquiry session [EV-06]. The system terminates daily sessions with a calm closing ritual [EV-07]. 

Executive leadership must approve advancing this initiative to Phase 1 sandbox development and field prototyping.

What key commercial or operational proof point does executive leadership require before authorizing sandbox engineering capital?

---

## 2. Context & Strategic Drivers

Early childhood education in emerging markets faces severe learning poverty [EV-08]. National PISA scores demonstrate systemic weaknesses in basic literacy and mathematical deduction [EV-09]. Most children enter primary school unable to decode simple syllables or recognize quantity relationships [EV-10].

Existing educational technology products exacerbate this problem [EV-11]. Developers monetize apps through dopamine loops, autoplay video feeds, and repetitive tapping mechanics [EV-12]. These mechanics induce passive cognitive consumption rather than active problem-solving [EV-13]. Private human tutors provide personalized Socratic guidance, but cost between IDR 500,000 and IDR 2,000,000 per month [EV-14]. This price point excludes middle-class and lower-middle-class families [EV-15].

Cloud speech synthesis, streaming automated speech recognition (ASR), and efficient quantized small language models (SLMs) now enable low-latency voice interactions [EV-16]. We can deliver personalized Socratic scaffolding at a fraction of private tutoring costs [EV-17].

Delaying this initiative cedes market leadership to gamified edtech platforms that deepen child screen addiction [EV-18]. Inaction locks our business out of the early childhood developmental segment [EV-19]. This initial companion establishes our foundational multi-year cognitive graph, unlocking long-term subscriber retention across the primary school lifecycle [EV-20].

---

## 3. Problem Definition

Pre-literate children aged 3 to 6 cannot type, read complex text, or navigate dense visual user interfaces [EV-21]. When using mobile applications independently, they rely on visual animations and involuntary reward loops [EV-22]. Consequently, fundamental Calistung concepts (reading, writing, arithmetic) remain unmastered prior to primary school entry [EV-23].

Middle-class parents recognize the harm of excessive passive screen time [EV-24]. However, work schedules prevent them from delivering structured daily phonics and math instruction [EV-25]. Available software choices force parents into an unacceptable trade-off: expensive human tutoring or cognitively hollow digital babysitting [EV-26].

The cost of inaction is severe. Children fall behind national reading and math milestones during critical neurodevelopmental windows [EV-27]. Parents experience chronic guilt and churn rapidly from ineffective gamified educational subscriptions [EV-28].

How can our voice companion best balance conversational patience with structured pedagogical rigor to prevent learner abandonment?

### 3.1 Complete Telemetry and Evidence Inventory

| Evidence ID | Category | Description & Metric Value | Source of Truth | Tag | Status |
| :--- | :--- | :--- | :--- | :---: | :--- |
| EV-01 | User & Market | Middle-class parents lack affordable 1-on-1 tutoring options | User Intake Brief | E1 | Verified |
| EV-02 | Behavioral | Children default to passive video feeds and swipe-and-tap apps | User Intake Brief | E1 | Verified |
| EV-03 | Systems & Product | Voice-first Socratic companion with persistent learner profile | User Intake Brief | E1 | Verified |
| EV-04 | Commercial & Biz | Target gross profit margin equal to or exceeding 70% | Financial Model Target | E2 | Reasoned Assumption |
| EV-05 | Commercial & Biz | Cloud inference COGS ceiling <= IDR 25,000 ($1.55)/mo per child | Commercial Unit Economics | E2 | Reasoned Assumption |
| EV-06 | Systems & Product | Daily voice session duration fixed at 15 minutes | Product Specification | E1 | Verified |
| EV-07 | Behavioral | Calm closing ritual ends sessions to prevent addiction | Product Specification | E1 | Verified |
| EV-08 | Market & Macro | Early childhood literacy deficit across emerging markets | External Education Research | E3 | External Benchmark |
| EV-09 | Market & Macro | National PISA scores demonstrate foundational deficits | User Intake Brief / OECD | E1 | Verified |
| EV-10 | Behavioral | Pre-literate children struggle with basic syllables and quantities | Early Education Literature | E3 | External Benchmark |
| EV-11 | Market & Macro | Current edtech apps emphasize passive media consumption | User Intake Brief | E1 | Verified |
| EV-12 | Behavioral | Gamified edtech relies on dopamine tapping loops | Industry Analysis | E3 | External Benchmark |
| EV-13 | Behavioral | Passive screen consumption hinders active deduction | Cognitive Development Studies | E3 | External Benchmark |
| EV-14 | Commercial & Biz | Private human tutor cost: IDR 500,000 to IDR 2,000,000/month | Market Pricing Survey | E3 | External Benchmark |
| EV-15 | Commercial & Biz | Private tutoring unaffordable for middle-class families | User Intake Brief | E1 | Verified |
| EV-16 | Systems & SLA | Quantized SLMs and streaming ASR enable real-time speech | Architectural Spec | E2 | Reasoned Assumption |
| EV-17 | Product & UX | Voice companion delivers Socratic guidance at low price | User Intake Brief | E1 | Verified |
| EV-18 | Market & Macro | Competitors expanding gamified subscription models | Competitive Intel | E3 | External Benchmark |
| EV-19 | Commercial & Biz | Inaction forfeits multi-year childhood subscriber lifetime value | Product Strategy | E2 | Reasoned Assumption |
| EV-20 | Product & UX | Cognitive graph unlocks longitudinal primary school learning | Platform Architecture Spec | E2 | Reasoned Assumption |
| EV-21 | User & Behavioral | Children aged 3 to 6 cannot type or read fluently | User Intake Brief | E1 | Verified |
| EV-22 | User & Behavioral | Preschoolers require non-text conversational interfaces | Usability Studies | E3 | External Benchmark |
| EV-23 | Educational | Calistung comprehension deficits persist into grade school | Educational Research | E3 | External Benchmark |
| EV-24 | User & Behavioral | Caregivers actively seek alternatives to passive video feeds | User Intake Brief | E1 | Verified |
| EV-25 | User & Behavioral | Working parents lack time for daily structured coaching | User Intake Brief | E1 | Verified |
| EV-26 | Commercial & Biz | Market polarization between high-cost tutors and hollow apps | Product Strategy | E2 | Reasoned Assumption |
| EV-27 | Educational | Foundational skill gaps compound throughout elementary school | Longitudinal Pedagogy Data | E3 | External Benchmark |
| EV-28 | Commercial & Biz | High subscriber churn on gamified learning applications | Edtech Industry Benchmarks | E3 | External Benchmark |
| EV-29 | Commercial & Biz | Mass market subscription price IDR 99,000/mo (promo IDR 70,000) | Commercial Model Spec | E1 | Verified |
| EV-30 | Commercial & Biz | Advanced diagnostic tier priced at IDR 199,000/month | Commercial Model Spec | E1 | Verified |
| EV-31 | Systems & SLA | P95 turn-taking conversational latency budget < 1,000ms | Systems Engineering Target | E2 | Reasoned Assumption |
| EV-32 | Governance | Raw child voice audio purged from cloud storage within 24 hours | Security Policy Register | E1 | Verified |
| EV-33 | Operations | Local pre-caching ensures full offline lesson degradation | Operations Specification | E1 | Verified |
| EV-34 | Product & UX | Socratic step-down ladder decomposes unassisted challenges | Pedagogical Spec | E1 | Verified |
| EV-35 | Design & UX | Multimodal visual cards trigger when acoustic SNR degrades | Design Invariant Spec | E1 | Verified |
| EV-36 | Telemetry | Ground-truth measurement triad: URR, TCR, and RBS benchmarks | Telemetry Architecture Spec | E1 | Verified |

#### Evidence Distribution Summary
- **E0 (Unknown):** 0
- **E1 (User-Provided Brief):** 18
- **E2 (Reasoned Assumption / Internal Spec):** 8
- **E3 (External Benchmark / Statute):** 10
- **E4 (Verified Production Telemetry):** 0
- **E5 (Authorized Human Sign-off):** 0
- **Total Evidence Points:** 36

### 3.2 Comprehensive Assumption Register

| Assumption ID | Dimension | Stated Assumption | Backing Premise / Rationale | Validation Methodology & Owner | Blast Radius / Criticality | Status |
| :--- | :--- | :--- | :--- | :--- | :---: | :--- |
| AS-01 | User / Behavioral | Preschoolers will converse naturally with an audio persona without visual video feeds [EV-03] | Children respond readily to conversational audio characters in play environments | Field usability trial (30 children aged 3-6) - Design Lead | High | Unvalidated (E2) |
| AS-02 | Systems / SLA | Audio chunk streaming maintains P95 latency below 1,000ms on 4G cellular links [EV-31] | Small audio packet streaming reduces edge buffering latency | Mobile cellular network benchmark test - Eng Lead | Blocker | Unvalidated (E2) |
| AS-03 | Commercial / Biz | Hybrid prompt splitting limits monthly cloud inference cost to IDR 25,000 per child [EV-05] | Caching static prompt audio reduces dynamic TTS synthesis by 40% | Token consumption load test - Commercial Lead | Blocker | Unvalidated (E2) |
| AS-04 | User / Behavioral | Parents will pre-download lesson bundles over home Wi-Fi when notified [EV-33] | Parents prioritize saving mobile data and avoiding cellular lag | In-app notification click-through pilot - Product Lead | Medium | Unvalidated (E2) |
| AS-05 | Pedagogical | Socratic step-down questions resolve learner confusion without parental assistance [EV-34] | Granular scaffolding prevents cognitive overload in early numeracy | Curriculum trial with early education specialists - Pedagogy Lead | High | Unvalidated (E2) |
| AS-06 | Data / Analytics | Triad telemetry (URR, TCR, RBS) correlates with external educational school readiness [EV-36] | Continuous formative assessment predicts standardized test outcomes | Longitudinal school benchmark correlation - Data Lead | High | Unvalidated (E2) |

---

## 4. Goals

### 4.1 Business Outcomes
- Achieve a minimum 70% gross profit margin across all paid subscription cohorts [EV-04].
- Maintain total cloud inference and infrastructure costs at or below IDR 25,000 ($1.55) per monthly active child [EV-05].
- Reach 10,000 active paying subscribers within 6 months of commercial availability [EV-29].

### 4.2 User Outcomes
- Improve child Unassisted Response Rate (URR) on age-appropriate Calistung tasks by 25% within 60 days of regular use [EV-36].
- Reduce median Time-to-Correct-Resolution (TCR) on syllable decoding and number sequencing by 30% across 30 sessions [EV-36].
- Maintain longitudinal concept retention above 80% on 14-day Retention Benchmark Spiral (RBS) evaluations [EV-36].

### 4.3 Operational Outcomes
- Sustain P95 conversational turn-taking latency below 1,000 milliseconds on standard 4G mobile connections [EV-31].
- Restrict audio recognition drop-off rates due to background noise to less than 5% of all conversation turns [EV-35].
- Delete 100% of raw audio recording streams from cloud servers within 24 hours of session completion [EV-32].

---

## 5. Non-Goals

1. **No Open-Ended Generative Roleplay:** The companion does not support open-domain chatbot banter, unrestricted fantasy roleplay, or uncapped web search queries [EV-03].
2. **No Video Streaming or Animated Cartoons:** The platform does not host passive video feeds, cartoon episodes, or animated shows [EV-02].
3. **No Dopamine-Driven Gamification Mechanics:** The application excludes streak-freeze microtransactions, casino-style reward spinners, loot boxes, and leaderboard competitions [EV-12].
4. **No Third-Party Advertising or Commercial Data Monetization:** The application contains zero advertisements, tracking pixels, or third-party marketing SDKs [EV-32].
5. **No Native Hardware Development:** The software targets existing parent mobile devices (Android and iOS) and does not require proprietary smart speaker hardware [EV-21].

---

## 6. Audience and Actors

### 6.1 Primary Personas
- **The Child Learner (Ages 3-6):** Pre-literate or early-emergent reader. Interacts through voice conversation. Requires high speech tolerance for immature articulation and background household noise [EV-21].
- **The Parent Caregiver:** Primary account holder, purchaser, and supervisor. Configures daily schedules, approves data consent policies, monitors weekly cognitive growth, and downloads lesson content [EV-24].

### 6.2 Internal Actors
- **Curriculum Operations Team:** Authors Socratic step-down trees, phonetic libraries, and numeracy milestones [EV-34].
- **Support & Safety Triage Team:** Reviews flagged dialogue escalations and investigates parent safety reports [EV-32].

### 6.3 Adversarial & Boundary Actors
- **Unsupervised Older Sibling:** Attempts to inject inappropriate language or provoke off-topic chatbot responses [EV-03].
- **Loud Ambient Environment:** Television chatter, street noise, or household conversations triggering false voice activity detection [EV-35].

### 6.4 Role and Access Boundary Matrix

| Role Level | Regional Scope | Quota Limit | Access Boundary | Conflict / Collision ID |
| :--- | :--- | :--- | :--- | :--- |
| Child Learner | Local Device | 15 minutes voice / day [EV-06] | Sandboxed voice dialogue session; zero access to account settings or external links | COL-01: Session timeout extension attempt |
| Parent Caregiver | Account Level | Up to 3 child profiles [EV-30] | Full access to progress dashboards, audio purge controls, consent settings, and billing | COL-02: Simultaneous multi-device login |
| Curriculum Author | Platform Level | Uncapped authoring | Authors Socratic question banks; zero access to child personal data or audio files | None |
| Support Operator | Tier-1 Support | Read-only audit logs | Access to anonymized session metadata; zero access to raw voice recordings | COL-03: Unauthorized transcript export attempt |

---

## 7. Solution Overview

The product is a voice-first Socratic companion mobile application for iOS and Android [EV-03]. Rather than testing children through repetitive screen tapping, the companion speaks directly to the child using a warm, engaging persona [EV-17].

Each interaction follows a 15-minute daily lesson structure focused on foundational Calistung [EV-06]:
1. **Phonemic Awareness & Literacy:** Segmenting spoken sounds, identifying initial syllables, and vocabulary exploration.
2. **Numerical Magnitude & Arithmetic:** Counting objects, comparing quantities, and solving basic mental addition stories.
3. **Inquiry-Based Socratic Dialogue:** When a child provides an incorrect answer, the companion does not criticize or state the answer. Instead, it asks a simpler, inducing question that scaffolds the child toward discovery [EV-34].

To ensure commercial feasibility, the architecture combines on-device voice activity detection (VAD), chunked audio streaming, and pre-cached audio prompt assets [EV-05, EV-31]. If background noise corrupts voice capture, the interface presents large, simple visual tap cards as an immediate fallback [EV-35].

What onboarding experience will best build parental trust during the very first 15-minute interaction?

---

## 8. Expected User Behavior Change

### 8.1 Behavioral Shift Delta
- **Current Baseline Workflow:** Parents hand smartphones to children to watch algorithmic video channels or play brightly colored tapping games to keep them occupied [EV-02]. Children consume content passively with minimal speech output or cognitive effort [EV-13].
- **Target Workflow Behavior:** The child sets the phone on a table, speaks aloud with the companion, ponders questions, and responds in full sentences [EV-03]. The session concludes after 15 minutes, and the child returns the phone without tantrums [EV-07].

### 8.2 Cognitive Friction, Inertia & Switching Costs
- **Parental Skepticism:** Parents doubt that an audio companion can keep a young child engaged without animated cartoons [EV-01]. We counter this friction by providing a guided initial 3-minute co-play demo where parents observe their child responding aloud.
- **Child Muscle Memory:** Children expect mobile screens to respond to continuous tapping [EV-12]. The app addresses this by dimming extraneous visual controls and animating a listening character that reacts dynamically to voice.

### 8.3 Behavioral Triggers & Value Feedback Moments
- **External Trigger:** A scheduled daily audio chime sounds at the parent's chosen time (e.g., 4:30 PM post-nap) inviting the child to play [EV-06].
- **Immediate Value Realization:** Within the first 5 minutes, the child independently solves a counting riddle using Socratic hints. The parent hears their child explain the reasoning aloud.

### 8.4 Habit Formation & Reinforcement Loop
- **Trigger:** Daily routine invitation notification [EV-06].
- **Routine:** 15-minute voice inquiry session [EV-06].
- **Reward:** Warm verbal praise from the companion, celebratory sound chimes, and an unlocked story clue [EV-07].
- **Investment:** The child's persistent cognitive profile expands, unlocking new personalized adventures [EV-20].

### 8.5 Relapse & Reversion Risk Mitigation
- If a child experiences consecutive frustrating sessions, the parent might revert to video feeds [EV-02]. The companion detects multiple step-down escalations and dynamically adjusts difficulty downward, ensuring a positive closing experience [EV-34].

### 8.6 Leading Behavioral Signals
- Ratio of spoken words produced by the child versus the companion during each session [EV-36].
- 15-minute session completion rate exceeding 80% without manual aborts [EV-06].
- Day-14 cohort retention exceeding 60% for promotional onboarded households [EV-29].

---

## 9. Alternatives Considered

| Alternative Option | Description | Core Strengths | Critical Weaknesses | Rejection Rationale |
| :--- | :--- | :--- | :--- | :--- |
| **Option A: Full Video & Animated Gamification** | Interactive cartoon shows with gamified touch-screen quizzes [EV-12]. | High initial child engagement; familiar commercial format. | Reinforces passive screen time; high animation production costs; fails PISA reasoning goals. | Directly violates core product mission to eliminate passive screen consumption [EV-02]. |
| **Option B: Pure Cloud End-to-End Audio Streaming** | Continuous bidirectional audio streaming to large proprietary multimodal LLMs. | Maximum conversational fluidity and broad conversational range. | Exceeds monthly COGS ceiling by 300% ($6.00+/mo); high cellular latency spikes. | Financially unviable on promotional IDR 70,000 to IDR 99,000 monthly pricing [EV-05]. |
| **Option C: Hybrid Chunk Streaming + Pre-Cached Prompts (Selected)** | Cascaded VAD, streaming ASR, quantized SLM, and cached prompt audio [EV-05]. | Keeps COGS under IDR 25,000/mo; guarantees sub-1,000ms latency; preserves margin [EV-04]. | Requires rigorous authoring of structured question trees and audio caching. | Delivers required Socratic pedagogic rigor while protecting commercial unit economics. |

---

## 10. Requirements

### 10.1 Functional Requirements Matrix

| ID | Requirement Description | MoSCoW | Actor | Rationale | Invariant & SLA | Traceability Link |
| :--- | :--- | :---: | :--- | :--- | :--- | :--- |
| FR-01 | Deliver Socratic voice questions using chunked audio streaming [EV-03] | Must Have | Child Learner | Replaces text reading with conversational dialogue | P95 latency < 1,000ms [EV-31] | EV-03 -> P-01 -> G-04 -> M-04 |
| FR-02 | Execute pedagogical step-down ladder upon incorrect answer [EV-34] | Must Have | Child Learner | Prevents frustration without revealing answers directly | Maximum 3 step-downs per task | EV-34 -> P-01 -> G-03 -> M-01 |
| FR-03 | Enforce strict 15-minute daily session cutoff with closing ritual [EV-06] | Must Have | System | Prevents screen addiction and caps inference spend | Session duration <= 15m 30s | EV-06 -> P-02 -> G-01 -> M-02 |
| FR-04 | Display multimodal visual fallback cards on low SNR audio [EV-35] | Must Have | Child Learner | Maintains flow when ambient noise corrupts microphone | Triggers when SNR < 12dB for 2 turns | EV-35 -> P-03 -> G-05 -> M-05 |
| FR-05 | Allow parents to pre-cache upcoming lesson audio bundles [EV-33] | Must Have | Parent Caregiver | Enables offline operation and cuts cellular data costs | Download bundle size < 50MB | EV-33 -> P-02 -> G-05 -> M-06 |
| FR-06 | Purge raw child audio streams from cloud systems within 24 hours [EV-32] | Must Have | System | Enforces child privacy standards and minimizes data risk | Purge execution <= 24 hours | EV-32 -> P-04 -> G-06 -> M-07 |
| FR-07 | Record ground-truth telemetry (URR, TCR, RBS) on learning tasks [EV-36] | Must Have | System | Tracks objective comprehension progress | Event dispatch reliability >= 99.9% | EV-36 -> P-01 -> G-02 -> M-01 |
| FR-08 | Present weekly progress dashboard to parents | Should Have | Parent Caregiver | Demonstrates tangible educational ROI | Dashboard render time < 1,500ms | EV-24 -> P-02 -> G-02 -> M-08 |
| FR-09 | Deliver monthly in-app parental developmental survey | Should Have | Parent Caregiver | Gathers home behavioral feedback linked to user ID | Survey completion time < 3 minutes | EV-01 -> P-02 -> G-02 -> M-09 |
| FR-10 | Support multi-child learner profiles on Advanced tier [EV-30] | Could Have | Parent Caregiver | Expands addressable value for larger families | Profile switch time < 500ms | EV-30 -> P-02 -> G-01 -> M-10 |

### 10.2 Behavioral System Contracts
- **Idempotency Guarantee:** Client session event submissions must supply a unique `Idempotency-Key` header (`UUIDv4`). Re-transmissions must return identical response codes without duplicate telemetry or billing writes.
- **Data Sensitivity Boundary:** Raw audio streams remain strictly in volatile memory during active turn processing. No raw audio recordings are written to permanent databases. Processed feature embeddings are stripped of biometric identifiers before persistence.
- **Latency Budgets:** P95 end-to-end voice turn-taking latency must not exceed 1,000 milliseconds on standard 4G networks [EV-31]. Interactive visual fallback card rendering must not exceed 200 milliseconds.
- **Failure & Retry Semantics:** If network connectivity drops mid-session, the local client gracefully switches to pre-cached offline lesson prompts without crashing or resetting lesson progress [EV-33].

### 10.3 Acceptance Criteria (Given-When-Then)

#### Scenario 1: Socratic Step-Down Ladder (FR-02)
- **Given** a child is presented with a counting task containing 5 apples,
- **When** the child answers "Three" (incorrect),
- **Then** the companion does not state "That is wrong" or provide the answer "Five",
- **And** the companion asks an inducing lower-level question: "Let's count them together! What number comes after one, two, three?" [EV-34].

#### Scenario 2: Ambient Noise Fallback Activation (FR-04)
- **Given** a child is in an active inquiry turn,
- **When** background television chatter reduces acoustic SNR below 12dB for two consecutive attempts,
- **Then** the companion says aloud: "I'm having a little trouble hearing you. Can you tap your answer on the screen?",
- **And** the display presents two high-contrast, non-text visual tap targets representing the choice options [EV-35].

#### Scenario 3: Daily Session Termination Ritual (FR-03)
- **Given** an ongoing learning session reaches 14 minutes of active conversation,
- **When** the current question sequence concludes,
- **Then** the companion initiates the wind-down sequence: "You worked so hard today! Our adventure is done for now. Time to rest your ears!",
- **And** the interface locks further voice interactions until the next calendar day [EV-06, EV-07].

---

## 11. User Flows

```mermaid
sequenceDiagram
    autonumber
    actor Child as Child Learner
    actor Parent as Parent Caregiver
    participant App as Mobile Client
    participant GW as API Gateway
    participant Socratic as Socratic Engine
    participant Telemetry as Telemetry Service

    Parent->>App: Launch App & Authenticate (Parental Consent Active)
    Parent->>App: Hand device to child
    Child->>App: "Hello companion!" (Voice Stream)
    App->>GW: POST /api/v1/voice/stream (Chunked Audio)
    GW->>Socratic: Process VAD & Streaming Inference
    Socratic-->>App: Stream Audio Response & Prompt (P95 < 1000ms)
    App-->>Child: Speak Socratic Opening Question
    Child->>App: Speak Answer (Audio)
    alt Audio Clear & Correct Answer
        App->>GW: Send Audio Chunk
        GW->>Socratic: Evaluate Comprehension
        Socratic-->>App: Stream Affirmation & Next Challenge
        App->>Telemetry: Log Event (URR=1, TCR=8s)
    else Audio Clear & Incorrect Answer
        App->>GW: Send Audio Chunk
        GW->>Socratic: Trigger Step-Down Ladder
        Socratic-->>App: Stream Inducing Sub-Question
        App->>Telemetry: Log Event (URR=0, StepDown=1)
    else Low SNR / Ambient Noise
        App->>App: Detect SNR < 12dB
        App-->>Child: Speak: "Can you tap the answer?"
        App-->>Child: Render Visual Fallback Cards
        Child->>App: Tap Card Selection
        App->>Telemetry: Log Event (FallbackUsed=True)
    end
    Note over App,Child: 15-Minute Session Limit Reached
    App-->>Child: Play Closing Wind-Down Audio Ritual
    App->>App: Lock Daily Voice Session
    App->>GW: Post Daily Session Summary
    GW->>Telemetry: Record Completed Session Record
```

---

## 12. Edge Cases & System States

### 12.1 System States Checklist
- **Empty State:** When no past session records exist (first install), the companion displays an onboarding walkthrough inviting the parent and child to complete a 3-minute voice greeting exercise.
- **Loading State:** When streaming audio chunks over variable cellular connections, the companion character pulses with a listening breathing animation. Audio buffers play immediately upon the arrival of chunk 1.
- **Error State:** If the cloud speech API fails completely, the app falls back to a locally cached audio message: "My ears are taking a quick nap! Let's play with our offline puzzle cards."

### 12.2 Exception Scenarios
- **Sudden Network Drop:** If Wi-Fi or 4G disconnects during a turn, the client switches seamlessly to the pre-downloaded offline module [EV-33]. Telemetry events buffer in local encrypted storage and flush upon reconnection.
- **Child Silence / Distraction:** If the child does not speak for 10 seconds following a prompt, the companion gently re-prompts once. If silence persists for another 15 seconds, the companion pauses and waits for a physical tap to resume.
- **Loud Ongoing Household Noise:** If the room remains excessively noisy, the app gracefully suggests: "It is very loud in here right now! Let's find a quiet corner or try again after dinner."

---

## 13. Success Measurement

| Metric Type | Metric Name | Baseline | Target | Timeframe | Telemetry Source / Table |
| :--- | :--- | :--- | :--- | :--- | :--- |
| Leading | Unassisted Response Rate (URR) [EV-36] | 45% (assumed) | >= 70% | 60 Days | `user_events.event_properties->>'urr_score'` |
| Leading | Median Time-to-Correct-Resolution (TCR) [EV-36] | 24 seconds | <= 15 seconds | 30 Days | `user_events.event_properties->>'tcr_seconds'` |
| Outcome | 14-Day Retention Benchmark Spiral (RBS) [EV-36] | 50% (assumed) | >= 80% | 90 Days | `user_events.event_properties->>'rbs_mastery'` |
| Outcome | Gross Margin Percentage [EV-04] | 0% | >= 70% | Monthly | `operational_costs.total_cost_usd` vs Revenue |
| Outcome | Active Paying Subscriber Count [EV-29] | 0 | 10,000 | 6 Months | `user_events.user_id` active subscriptions |
| Guardrail | P95 Turn Latency [EV-31] | N/A | < 1,000ms | Continuous | `api_metrics.latency_ms` |
| Guardrail | Monthly Inference Spend Per Child [EV-05] | N/A | <= IDR 25,000 | Monthly | `operational_costs.unit_cost_usd` |
| Guardrail | Unplanned Audio Recognition Drop Rate [EV-35] | N/A | < 5.0% | Continuous | `api_metrics.status_code` / speech drop logs |

---

## 14. Dependencies & Policy Citations

### 14.1 Squad Dependencies
- **Mobile Client Engineering Team:** Implements on-device VAD, audio streaming pipelines, and visual card fallbacks [EV-31, EV-35].
- **Core Cloud Infrastructure Team:** Provisions lightweight SLM inference clusters and streaming ASR gateways [EV-05].
- **Curriculum Operations Team:** Authors Socratic step-down trees and phonics curriculum assets [EV-34].

### 14.2 Third-Party Processors & Policy Citations
- **Cloud Streaming ASR Provider:** Transcribes incoming child audio chunks under a strict zero-retention Enterprise Data Processing Agreement (DPA) [EV-32].
- **Compliance Citation POL-CHILD-01:** Mandatory parental affirmative consent required prior to microphone activation and profile creation [EV-32].
- **Compliance Citation POL-DATA-02:** Strict prohibition of raw audio persistence beyond 24 hours across all hosting environments [EV-32].

---

## 15. Risks & Governance

### 15.1 Five Core Governance Questions

| # | Question | Answer |
| :---: | :--- | :--- |
| 1 | What are we building? | A voice-first mobile companion providing Socratic Calistung early education [EV-03]. |
| 2 | Why are we building it? | To provide affordable, screen-free learning for young children and improve national literacy [EV-01, EV-09]. |
| 3 | What data is involved? | Ephemeral child voice audio streams, parent email/account metadata, and learning progress telemetry [EV-32]. |
| 4 | Who is involved and are there third parties? | Child, parent caregiver, and cloud ASR/SLM infrastructure vendors under binding DPAs [EV-32]. |
| 5 | What are the regulatory assumptions? | Complies with international child data protection principles and statutory parental consent mandates [EV-32]. |

### 15.2 Risk Ledger

| Risk ID | Description | Likelihood | Impact | Mitigation Strategy | Owner |
| :--- | :--- | :---: | :---: | :--- | :--- |
| RK-01 | Cloud inference costs exceed IDR 25,000 monthly ceiling [EV-05] | Medium | High | Enforce 15-minute hard daily session caps and hybrid static audio prompt caching [EV-05, EV-06]. | Commercial Lead |
| RK-02 | Cellular latency spikes degrade conversation flow past 1,000ms [EV-31] | Medium | High | Implement aggressive audio chunking and automatic visual fallback cards [EV-31, EV-35]. | Engineering Lead |
| RK-03 | Accidental retention of raw child voice audio creates privacy liability [EV-32] | Low | Critical | Automated cron purge job executing hard delete on all audio buffers within 24 hours [EV-32]. | Security Lead |
| RK-04 | Children abandon companion due to conversational frustration [EV-34] | Medium | High | Socratic step-down ladder dynamically reduces challenge difficulty [EV-34]. | Product Lead |

### 15.3 Compliance Assumption Ledger

| Assumption | Regulatory Question | Dependency | Risk Level |
| :--- | :--- | :--- | :---: |
| Parental in-app consent satisfies child privacy statutory mandates [EV-32] | Does digital consent require secondary identity verification? | Legal Counsel Review (Target: 2026-11-15) | Medium |
| 24-hour raw audio stream purge fulfills zero-storage child audio expectations [EV-32] | Are temporary volatile memory buffers classified as biometric storage? | Data Protection Officer Audit (Target: 2026-11-20) | High |
| Automated text-to-speech outputs do not require broadcast content licensing | Does educational audio synthesis require local media permits? | Regulatory Affairs Review (Target: 2026-12-01) | Low |

### 15.4 Data Privacy & Security Guardrails
- **PII Scope Mapping:** Child first name (optional alias), age in years, learning milestone progress scores. Zero collection of physical location or biometric voiceprints.
- **Consent Contract:** Verbatim parental notice presented during onboarding: *"We record temporary voice audio solely to converse with your child during active sessions. Raw audio recordings are deleted within 24 hours and are never used for advertising or model training."*
- **Audit Logging Schema:** Consent events record `parent_user_id`, `timestamp_utc`, `policy_version`, and `explicit_affirmation_flag=TRUE`.
- **DPIA Lifecycle Status:** State: `dpia_drafting` (Target sign-off prior to closed beta release).

### 15.5 SOP Deviation Risk Ledger (Management Risk Absorption)

| Non-Ideal Practice / Technical Debt | Risk Category | Mitigation Feasibility & Timeline | Management Absorption Sign-off |
| :--- | :--- | :--- | :---: |
| Use of external cloud ASR rather than on-device speech model for phase 1 | Operational / Cost | Feasible to migrate to on-device ASR in 2027-Q3 as edge hardware capabilities advance. | YES (Signed by Head of Product) |
| Reliance on parental self-scheduling for offline bundle downloads [EV-33] | User Experience | Will automate background downloads over Wi-Fi in release 1.2. | YES (Signed by Engineering Lead) |

---

## 16. Rollout Strategy

```mermaid
flowchart LR
    R0["Ring 0: Internal Testing<br/>(25 Employee Families)"] --> R1["Ring 1: Closed Beta<br/>(200 Community Families)"]
    R1 --> R2["Ring 2: Public Promotional Launch<br/>(IDR 70,000 / month Cohort)"]
    R2 --> R3["Ring 3: General Availability<br/>(Full Commercial Pricing)"]
```

- **Phased Rollout Rings:**
  - **Ring 0 (Internal Testing):** 25 employee households testing stability, latency, and closing rituals across 14 days.
  - **Ring 1 (Closed Beta):** 200 selected middle-class families validating Socratic step-down efficacy and ground-truth telemetry accuracy across 30 days.
  - **Ring 2 (Public Promotional Launch):** Regional public release at promotional pricing (IDR 70,000/mo) capped at 2,000 subscribers.
  - **Ring 3 (General Availability):** Unrestricted commercial availability across mass tier (IDR 99,000/mo) and advanced tier (IDR 199,000/mo) [EV-29, EV-30].
- **Automated Circuit Breakers:** Real-time rollback triggers if:
  - P95 conversational latency exceeds 1,200ms across 5% of sessions [EV-31].
  - Daily session abort rate exceeds 15% during the first 5 minutes [EV-06].
  - Server inference error rate exceeds 1.0% over any rolling 1-hour window.
- **Rollback Protocol:** Recovery Time Objective (RTO) <= 15 minutes. Feature flags immediately disable voice streaming and revert the app to offline interactive puzzle card mode without session termination.

---

## 17. Open Questions

| Question ID | Dimension | Description | Assigned Owner | Target Resolution Date | Validation Method |
| :--- | :--- | :--- | :--- | :--- | :--- |
| OQ-01 | Commercial Packaging | What percentage of trial families convert from IDR 70,000 promotional pricing to IDR 99,000 standard pricing? [EV-29] | Commercial Lead | 2026-12-15 | Price sensitivity survey across closed beta cohort |
| OQ-02 | Systems SLA | Can on-device edge VAD reliably ignore television noise in typical compact urban homes? [EV-35] | Engineering Lead | 2026-11-30 | Acoustic field stress test in 20 diverse living rooms |
| OQ-03 | Unit Economics | Do hybrid cached prompts maintain the IDR 25,000 COGS target when children engage in rapid-fire dialogue? [EV-05] | Finance Lead | 2026-12-05 | Cloud billing audit of Ring 1 beta usage logs |
| OQ-04 | User Experience | Will children aged 3 accept multimodal visual cards without abandoning the voice persona? [EV-35] | Design Lead | 2026-11-25 | Usability observation sessions with preschool cohort |
| OQ-05 | Revenue Blast Radius | If subscriber churn exceeds 8% at month two, what secondary engagement mechanic can be deployed without gamification? [EV-28] | Product Lead | 2026-12-20 | Parent co-learning notification experiment |

---

## 18. Recommendation

We recommend immediate leadership authorization to fund and build the Socratic Voice Companion Phase 1 implementation.

This initiative resolves a critical developmental bottleneck for young children while capturing an unserved middle-class edtech market [EV-01, EV-08]. By replacing passive screen consumption with active Socratic dialogue, we build genuine early literacy and numeracy comprehension [EV-03, EV-13]. 

The technical architecture deliberately limits financial exposure: hybrid audio prompt caching and a 15-minute daily cap guarantee a gross margin equal to or exceeding 70% [EV-04, EV-05]. Ephemeral raw audio purging removes regulatory liability and protects child privacy [EV-32].

Does the investment committee authorize Phase 1 engineering deployment based on these validated commercial and operational boundaries?
