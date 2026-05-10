export default function StatCard({
  icon: Icon, label, value, sub, accent, delay = "",
}: {
  icon: React.ElementType; label: string; value: string; sub?: string; accent: string; delay?: string;
}) {
  return (
    <div className={`bento-stat glass-card rounded-[22px] p-5 flex flex-col gap-3 anim-in ${delay}`} style={{ color: accent }}>
      <div className="w-9 h-9 rounded-[11px] flex items-center justify-center" style={{ background: `${accent}14` }}>
        <Icon className="w-4 h-4" style={{ color: accent }} />
      </div>
      <div>
        <p className="text-[10px] font-extrabold uppercase tracking-[0.2em] text-black/30 mb-0.5">{label}</p>
        <p className="font-serif font-black text-[28px] leading-none text-[var(--navy)]">{value}</p>
        {sub && <p className="text-[11px] text-black/30 font-medium mt-1">{sub}</p>}
      </div>
    </div>
  );
}