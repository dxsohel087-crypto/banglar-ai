using UnityEngine;
using UnityEngine.EventSystems;

namespace BangladeshLifeSimulator
{
    /// <summary>
    /// Lightweight UI joystick for touch and mouse testing.
    /// Attach to a UI object with a RectTransform. Assign the handle.
    /// </summary>
    public sealed class SimpleMobileJoystick : MonoBehaviour, IPointerDownHandler, IDragHandler, IPointerUpHandler
    {
        [SerializeField] private RectTransform handle;
        [SerializeField, Range(0.1f, 1f)] private float handleRange = 0.65f;

        public Vector2 Direction { get; private set; }

        private RectTransform root;

        private void Awake()
        {
            root = transform as RectTransform;
            if (root == null)
                Debug.LogError("SimpleMobileJoystick must be attached to a UI RectTransform.", this);
        }

        public void OnPointerDown(PointerEventData eventData) => OnDrag(eventData);

        public void OnDrag(PointerEventData eventData)
        {
            if (root == null || handle == null)
                return;

            if (!RectTransformUtility.ScreenPointToLocalPointInRectangle(
                    root, eventData.position, eventData.pressEventCamera, out Vector2 localPoint))
                return;

            Vector2 halfSize = root.rect.size * 0.5f;
            if (halfSize.x <= 0f || halfSize.y <= 0f)
                return;

            Direction = new Vector2(localPoint.x / halfSize.x, localPoint.y / halfSize.y);
            Direction = Vector2.ClampMagnitude(Direction, 1f);
            handle.anchoredPosition = new Vector2(
                Direction.x * halfSize.x * handleRange,
                Direction.y * halfSize.y * handleRange);
        }

        public void OnPointerUp(PointerEventData eventData)
        {
            Direction = Vector2.zero;
            if (handle != null)
                handle.anchoredPosition = Vector2.zero;
        }
    }
}
