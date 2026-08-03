import { chromium } from 'playwright';
import fs from 'fs';
import path from 'path';
import crypto from 'crypto';
import { fileURLToPath } from 'url';

const __dirname = path.dirname(fileURLToPath(import.meta.url));
const root = path.resolve(__dirname, '..');
const targetsPath = path.join(root, '.franky', 'targets.toml');
const visualDir = path.join(root, '.franky', 'visual');
const baselineDir = path.join(root, '.franky', 'baselines');
const reportPath = path.join(root, '.franky', 'visual-report.json');

const updateBaseline = process.argv.includes('--update-baseline');

function parseTargets(text) {
  const port = Number((text.match(/^port\s*=\s*(\d+)/m) || [])[1] || 3000);
  const routesMatch = text.match(/routes\s*=\s*\[(.*?)\]/s);
  let routes = ['/'];
  if (routesMatch) {
    routes = [...routesMatch[1].matchAll(/"([^"]+)"/g)].map((m) => m[1]);
    if (routes.length === 0) routes = ['/'];
  }
  return { port, routes };
}

function sha256(file) {
  return crypto.createHash('sha256').update(fs.readFileSync(file)).digest('hex');
}

function routeFile(route) {
  const safe = route.replace(/\//g, '_').replace(/^_/, '') || 'root';
  return `${safe}.png`;
}

async function main() {
  if (!fs.existsSync(targetsPath)) {
    console.error('visual: no targets.toml');
    process.exit(1);
  }
  const targets = parseTargets(fs.readFileSync(targetsPath, 'utf8'));
  fs.mkdirSync(visualDir, { recursive: true });
  fs.mkdirSync(baselineDir, { recursive: true });

  const baseUrl = `http://127.0.0.1:${targets.port}`;
  const browser = await chromium.launch({ headless: true });
  const page = await browser.newPage();
  const routes = [];
  let passed = true;

  for (const route of targets.routes) {
    const url = `${baseUrl}${route}`;
    const shotName = routeFile(route);
    const shotPath = path.join(visualDir, shotName);
    const baselinePath = path.join(baselineDir, shotName);
    let ok = true;
    let message = 'captured';

    try {
      const resp = await page.goto(url, { waitUntil: 'networkidle', timeout: 15000 });
      if (!resp || !resp.ok()) {
        ok = false;
        message = `HTTP ${resp ? resp.status() : 'no response'} for ${url}`;
        passed = false;
      } else {
        await page.screenshot({ path: shotPath, fullPage: true });
        if (updateBaseline || !fs.existsSync(baselinePath)) {
          fs.copyFileSync(shotPath, baselinePath);
          message = updateBaseline ? 'baseline updated' : 'baseline created';
        } else {
          const a = sha256(shotPath);
          const b = sha256(baselinePath);
          if (a !== b) {
            ok = false;
            message = `visual diff: ${shotName} hash mismatch (run --update-baseline if intentional)`;
            passed = false;
          } else {
            message = 'matches baseline';
          }
        }
      }
    } catch (err) {
      ok = false;
      message = String(err.message || err);
      passed = false;
    }

    routes.push({ route, screenshot: shotPath, ok, message });
  }

  await browser.close();

  const report = { version: '2', passed, routes, generated_at: new Date().toISOString() };
  fs.writeFileSync(reportPath, JSON.stringify(report, null, 2));
  console.log(JSON.stringify(report));
  process.exit(passed ? 0 : 1);
}

main();
