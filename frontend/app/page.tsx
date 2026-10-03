import Link from "next/link";

export default function Home() {
  return (
    <main style={{ maxWidth: 375, margin: "0 auto", padding: 24 }}>
      <h1>메디큐브톡</h1>
      <p>커뮤니티 화면을 준비 중입니다.</p>
      <Link href="/dev-user">개발용 유저 전환</Link>
    </main>
  );
}
