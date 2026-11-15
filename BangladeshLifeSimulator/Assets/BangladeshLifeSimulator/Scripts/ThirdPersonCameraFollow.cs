using UnityEngine;

namespace BangladeshLifeSimulator
{
    public sealed class ThirdPersonCameraFollow : MonoBehaviour
    {
        [SerializeField] private Transform target;
        [SerializeField] private Vector3 offset = new Vector3(0f, 2.4f, -4.2f);
        [SerializeField, Min(0f)] private float followSmoothness = 10f;
        [SerializeField, Min(0f)] private float lookHeight = 1.4f;

        private void LateUpdate()
        {
            if (target == null)
                return;

            Vector3 desiredPosition = target.TransformPoint(offset);
            transform.position = Vector3.Lerp(
                transform.position, desiredPosition, followSmoothness * Time.deltaTime);

            Vector3 lookPoint = target.position + Vector3.up * lookHeight;
            transform.rotation = Quaternion.Slerp(
                transform.rotation,
                Quaternion.LookRotation(lookPoint - transform.position, Vector3.up),
                followSmoothness * Time.deltaTime);
        }

        public void SetTarget(Transform newTarget) => target = newTarget;
    }
}
