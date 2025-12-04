## 1. Summary & Context
- What is being done and why.
**Example:** Add caching to user profile API to reduce DB load.

---

## 2. Requirements / Acceptance Criteria
- Clear, testable conditions and expected behaviors.
**Example:**
- [ ] Cache profiles for 5 minutes  
- [ ] Invalidate cache on update  
- [ ] Fallback to DB if cache fails

---

## 3. Technical Approach
- Architecture notes, design choices, implementation steps, performance considerations.
**Schematic Example:**
- Follow OOP principles and SOLID design practices.
  Client → API → Cache → DB

---

## 4. Testing & Definition of Done
- Test plan, validations, and completion checklist.
**Example:**
- Unit + integration tests  
- Documentation updated  
- Code reviewed and deployed to staging
