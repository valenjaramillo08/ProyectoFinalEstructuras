export default function LoadingSpinner() {
  return (
    <div className="flex flex-col gap-3 w-full" aria-live="polite" aria-busy="true">
      <div className="flex flex-col gap-3" aria-hidden="true">
        <div className="h-4 skeleton w-3/4" />
        <div className="h-[72px] skeleton w-full" />
        <div className="h-4 skeleton w-1/2" />
      </div>
    </div>
  );
}